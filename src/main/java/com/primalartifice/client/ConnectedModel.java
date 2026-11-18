package com.primalartifice.client;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;

/**
 * Connected glass (compact CTM): a full cube whose frame disappears where it touches the same glass, so a wall reads as
 * one pane. Each face is cut into four 8x8 quadrants, and each quadrant takes the same quadrant of one of five tiles,
 * chosen by the two side neighbours and the diagonal: {@code all} (the full frame, nothing joined), {@code horizontal}
 * (top/bottom frame only), {@code vertical} (left/right frame only), {@code corners} (inner-corner pixels only) and
 * {@code inner} (no frame).
 *
 * <p>Blockstate JSON: {@code {"type": "primalartifice:connected", "connects_to": "#ns:tag", "textures": {...}}}.
 * A neighbour joins when it is in {@code connects_to} and its own face on that side is not covered by the same tag.
 *
 * <p>The render layer comes from the textures, as for a plain cube in 26.1: the transparency of all five tiles together
 * (so a block never splits across layers), or translucent when a texture sets {@code force_translucent}. A tile whose
 * texture is missing falls back to {@code all}, so the block looks like the plain frame until its CTM art is in.
 */
public final class ConnectedModel implements DynamicBlockStateModel {
    // Quadrant tile, in the order of Textures' fields.
    private static final int ALL = 0, HORIZONTAL = 1, VERTICAL = 2, CORNERS = 3, INNER = 4;
    /**
     * 5^i: the place value of quadrant i (face * 4 + quadrant) in a part key. Base 5, not 3-bit fields: 24 x 3 bits is
     * 72 bits, which wraps round in a long and crashed L-bends in Magia Naturalis before 0.8.2 (5^24 fits in 56 bits).
     */
    private static final long[] POW5 = new long[24];

    static {
        POW5[0] = 1;
        for (int i = 1; i < POW5.length; i++) {
            POW5[i] = POW5[i - 1] * 5;
        }
    }

    private final TagKey<Block> connectsTo;
    /** [face][quadrant][tile]: quadrants are top-left, top-right, bottom-left, bottom-right as the texture reads. */
    private final BakedQuad[][][] quads;
    private final Material.Baked particle;
    private final Map<Long, BlockStateModelPart> parts = new ConcurrentHashMap<>();

    private ConnectedModel(TagKey<Block> connectsTo, BakedQuad[][][] quads, Material.Baked particle) {
        this.connectsTo = connectsTo;
        this.quads = quads;
        this.particle = particle;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
                             List<BlockStateModelPart> out) {
        out.add(part(key(level, pos)));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> out) {
        out.add(part(0L));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new GeometryKey(this, key(level, pos));
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return part(0L).materialFlags();
    }

    /** One base-5 digit per quadrant (its tile), 24 quadrants; 0 is the full frame everywhere. */
    private long key(BlockAndTintGetter level, BlockPos pos) {
        long key = 0;
        for (Direction face : Direction.values()) {
            Direction up = up(face), right = right(face);
            for (int q = 0; q < 4; q++) {
                Direction v = q < 2 ? up : up.getOpposite();
                Direction h = (q & 1) == 0 ? right.getOpposite() : right;
                boolean joinV = joins(level, pos.relative(v), face), joinH = joins(level, pos.relative(h), face);
                int tile = joinV && joinH ? (joins(level, pos.relative(v).relative(h), face) ? INNER : CORNERS)
                        : joinV ? VERTICAL : joinH ? HORIZONTAL : ALL;
                key += tile * POW5[face.ordinal() * 4 + q];
            }
        }
        return key;
    }

    private boolean joins(BlockAndTintGetter level, BlockPos at, Direction face) {
        return level.getBlockState(at).is(connectsTo) && !level.getBlockState(at.relative(face)).is(connectsTo);
    }

    private BlockStateModelPart part(long key) {
        return parts.computeIfAbsent(key, k -> {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (Direction face : Direction.values()) {
                for (int q = 0; q < 4; q++) {
                    builder.addCulledFace(face, quads[face.ordinal()][q][(int) (k / POW5[face.ordinal() * 4 + q] % 5)]);
                }
            }
            return new SimpleModelWrapper(builder.build(), true, particle);
        });
    }

    /** The world direction that is "up" on the face's texture (vanilla's default face UVs). */
    private static Direction up(Direction face) {
        return switch (face) {
            case DOWN -> Direction.SOUTH;
            case UP -> Direction.NORTH;
            default -> Direction.UP;
        };
    }

    /** The world direction that is "right" on the face's texture. */
    private static Direction right(Direction face) {
        return switch (face) {
            case DOWN, UP, SOUTH -> Direction.EAST;
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
        };
    }

    /** The element corners whose default UVs are the texture rectangle u0..u1, v0..v1 on this face. */
    private static Vector3f[] corners(Direction face, float u0, float v0, float u1, float v1) {
        return switch (face) {
            case DOWN -> new Vector3f[]{new Vector3f(u0, 0, 16 - v1), new Vector3f(u1, 0, 16 - v0)};
            case UP -> new Vector3f[]{new Vector3f(u0, 16, v0), new Vector3f(u1, 16, v1)};
            case NORTH -> new Vector3f[]{new Vector3f(16 - u1, 16 - v1, 0), new Vector3f(16 - u0, 16 - v0, 0)};
            case SOUTH -> new Vector3f[]{new Vector3f(u0, 16 - v1, 16), new Vector3f(u1, 16 - v0, 16)};
            case WEST -> new Vector3f[]{new Vector3f(0, 16 - v1, u0), new Vector3f(0, 16 - v0, u1)};
            case EAST -> new Vector3f[]{new Vector3f(16, 16 - v1, 16 - u1), new Vector3f(16, 16 - v0, 16 - u0)};
        };
    }

    private record GeometryKey(ConnectedModel model, long key) {
    }

    public record Textures(Material all, Material horizontal, Material vertical, Material corners, Material inner) {
        static final MapCodec<Textures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Material.CODEC.fieldOf("all").forGetter(Textures::all),
                Material.CODEC.fieldOf("horizontal").forGetter(Textures::horizontal),
                Material.CODEC.fieldOf("vertical").forGetter(Textures::vertical),
                Material.CODEC.fieldOf("corners").forGetter(Textures::corners),
                Material.CODEC.fieldOf("inner").forGetter(Textures::inner)
        ).apply(i, Textures::new));

        List<Material> inOrder() {
            return List.of(all, horizontal, vertical, corners, inner);
        }
    }

    public record Unbaked(TagKey<Block> connectsTo, Textures textures) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("connects_to").forGetter(Unbaked::connectsTo),
                Textures.CODEC.fieldOf("textures").forGetter(Unbaked::textures)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            List<Material> materials = textures.inOrder();
            Material.Baked[] baked = new Material.Baked[materials.size()];
            for (int t = 0; t < baked.length; t++) {
                Material m = materials.get(t);
                baked[t] = baker.materials().get(m, () -> "connected " + connectsTo.location() + " " + m.sprite());
            }
            // A tile whose texture is not there yet shows the full frame instead of the missing-texture checkerboard.
            for (int t = 1; t < baked.length; t++) {
                if (baked[t].sprite().contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                    baked[t] = baked[ALL];
                }
            }
            // One layer for the whole block: the transparency of every tile together.
            Transparency transparency = Transparency.NONE;
            for (Material.Baked b : baked) {
                transparency = transparency.or(b.forceTranslucent() ? Transparency.TRANSLUCENT
                        : b.sprite().contents().computeTransparency(0, 0, 1, 1));
            }
            BakedQuad.MaterialInfo[] infos = new BakedQuad.MaterialInfo[baked.length];
            for (int t = 0; t < baked.length; t++) {
                infos[t] = baker.interner().materialInfo(BakedQuad.MaterialInfo.of(baked[t], transparency, -1, true, 0, true));
            }
            BakedQuad[][][] quads = new BakedQuad[6][4][baked.length];
            for (Direction face : Direction.values()) {
                for (int q = 0; q < 4; q++) {
                    float u0 = (q & 1) * 8, v0 = (q >> 1) * 8;
                    Vector3f[] c = corners(face, u0, v0, u0 + 8, v0 + 8);
                    for (int t = 0; t < baked.length; t++) {
                        quads[face.ordinal()][q][t] = FaceBakery.bakeQuad(baker.interner(), c[0], c[1],
                                FaceBakery.defaultFaceUV(c[0], c[1], face), Quadrant.R0, infos[t], face,
                                BlockModelRotation.IDENTITY, null, ExtraFaceData.DEFAULT);
                    }
                }
            }
            return new ConnectedModel(connectsTo, quads, baked[ALL]);
        }
    }
}

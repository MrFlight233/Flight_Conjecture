package org.flight.flight_conjecture.blockentity.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.flight.flight_conjecture.container.menu.IndustrialProcessingUnitMenu;
import org.flight.flight_conjecture.init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class IndustrialProcessingUnitBlockEntity extends BlockEntity implements MenuProvider {

    /**
     * 一个教学用的示例字段。
     * <p>
     * progress 在本章并不代表真实机器逻辑，
     * 它只是一个“计数器”，用于验证：
     * BlockEntity 是否在 tick
     * 数据是否能被保存
     * 数据是否能在重进世界后恢复
     */
    private int progress = 0;

    public int getProgress() {
        return progress;
    }

    /**
     * 用于 Menu 与客户端同步数据的容器。
     * <p>
     * ContainerData 的作用是把 BlockEntity 中的整数数据
     * 暴露给 Menu 系统，从而在客户端与服务端之间自动同步。
     * <p>
     * 在本例中我们只同步一个字段：
     * index = 0  → progress
     * <p>
     * 如果以后需要同步更多数据（例如最大进度、能量等），
     * 只需要增加新的 index 即可。
     */
    protected final ContainerData data = new ContainerData() {

        /**
         * Menu 读取数据时调用。
         * 根据 index 返回对应的数据值。
         */
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                default -> 0;
            };
        }

        /**
         * Menu 写入数据时调用。
         * 客户端同步数据时会通过这里写回。
         */
        @Override
        public void set(int index, int value) {
            if (index == 0) progress = value;
        }

        /**
         * 返回需要同步的数据数量。
         *
         * 因为这里只有 progress 一个变量，
         * 所以返回 1。
         */
        @Override
        public int getCount() {
            return 1;
        }
    };

    /**
     * 工业处理单元的方块实体（BlockEntity）。
     * <p>
     * BlockEntity 用于为方块提供“可存储的数据与运行逻辑”。
     * 与普通 Block 不同，它可以：
     * - 保存数据（NBT）
     * - 在每 tick 执行逻辑
     * - 在世界重新加载后恢复状态
     *
     * @param pPos        方块在世界中的位置
     * @param pBlockState 当前方块状态（BlockState）
     */
    public IndustrialProcessingUnitBlockEntity(BlockPos pPos, BlockState pBlockState) {
        // 绑定 BlockEntityType + 世界坐标 + 当前状态
        // 这一步决定：
        //   1. 它属于哪种实体类型
        //   2. 它附着在哪个位置
        //   3. 它对应的方块状态是什么
        super(ModBlockEntities.INDUSTRIAL_PROCESSING_UNIT_BE.get(), pPos, pBlockState);
    }

    /**
     * 写入存档数据（NBT）。
     * <p>
     * 当世界保存或区块卸载时调用。
     * 只有在这里写入的数据，才能在重进世界后恢复。
     */
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        // 将 progress 写入 NBT
        // 键名 "Progress"，在 load 里也必须与此保持一致
        pTag.putInt("Progress", progress);

        // 将内部物品栏序列化后写入 NBT
        // "inventory" 是这一组库存数据在存档中的键名
        pTag.put("inventory", itemHandler.serializeNBT());
    }

    /**
     * 从存档读取数据（NBT）。
     * <p>
     * 当区块加载或方块实体被重建时调用。
     * 必须与 saveAdditional 使用相同的键名。
     */
    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        // 从 NBT 中读取 progress
        // 键名必须与 saveAdditional 里的键名相同
        progress = pTag.getInt("Progress");

        // 从 NBT 中读取库存数据并恢复到 itemHandler
        // 记住键名必须与 saveAdditional 中保持一致
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    /**
     * 每游戏刻执行一次（前提是 Block 中注册了 ticker）。
     * <p>
     * 这里我们让 progress 每 tick 自增，
     * 用于证明 BlockEntity 正在参与游戏循环。
     * <p>
     * setChanged() 表示数据已被修改，
     * 告诉游戏该实体需要被保存。
     */
    public void tick() {
        progress++;
        setChanged();
    }

    /**
     * 教学用调试方法。
     * <p>
     * 在没有 GUI 的情况下，
     * 通过聊天信息输出当前进度，
     * 用于验证 tick 与 NBT 是否正常工作。
     */
    public Component getDebugMessages() {
        return Component.literal("Progress: " + progress);
    }

    // 返回界面标题，决定 GUI 显示的标题名称
    @Override
    public Component getDisplayName() {
        return Component.translatable("be.title.industrial_processing_unit");
    }

    // 当玩家打开界面时创建 Menu
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new IndustrialProcessingUnitMenu(id, inventory, this, data);
    }

    // 输入槽索引
    private static final int INPUT_SLOT = 0;

    // 输出槽索引
    private static final int OUTPUT_SLOT = 1;

    /**
     * 工业处理单元的内部物品栏。
     * <p>
     * 这里使用 Forge 提供的 ItemStackHandler 作为库存实现。
     * 当前机器一共拥有两个槽位：
     * 0 -> 输入槽
     * 1 -> 输出槽
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {

        /**
         * 当某个槽位内容发生变化时调用。
         *
         * 这里调用 setChanged()，告诉游戏：
         * 当前 BlockEntity 的数据已经发生修改，需要被标记为“已更改”，
         * 这样世界保存时才会把新数据写入存档。
         */
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        /**
         * 控制某个槽位是否允许放入指定物品。
         *
         * 当前实现中：
         * 输入槽允许放入物品
         * 输出槽不允许手动放入物品
         *
         * 这正符合大多数机器的常见逻辑：
         * 玩家把原料放进输入槽而非输出槽，产物只会出现在输出槽。
         */
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT;
        }
    };

    /**
     * 返回当前机器内部的物品处理器。
     * <p>
     * Menu 会通过这个方法获取库存，
     * 再基于它创建真正的 GUI 槽位。
     */
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * 将当前机器内部的所有物品掉落到世界中。
     * <p>
     * ItemStackHandler 不是 Containers.dropContents 直接支持的容器类型，
     * 因此这里先创建一个临时的 SimpleContainer，
     * 再把 itemHandler 中的物品逐个拷贝进去，
     * 最后统一掉落。
     */
    public void drops() {
        // 创建一个临时容器，大小与机器槽位数量一致
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());

        // 将 itemHandler 中的每个槽位内容复制到临时容器中
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        // 将容器中的物品掉落到世界
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

}

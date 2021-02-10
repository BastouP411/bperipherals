package fr.bastoup.bperipherals.peripherals.magcardreader;

import dan200.computercraft.api.peripheral.IComputerAccess;
import fr.bastoup.bperipherals.init.ModItems;
import fr.bastoup.bperipherals.init.ModTileTypes;
import fr.bastoup.bperipherals.util.tiles.TilePeripheral;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

import java.util.UUID;

public class TileMagCardReader extends TilePeripheral {

    private byte[] write = null;
    private BlockStateMagCardReader state = BlockStateMagCardReader.READ;

    public TileMagCardReader() {
        super(ModTileTypes.MAG_CARD_READER);
        this.setPeripheral(new PeripheralMagCardReader(this));
    }

    public void magSwipe(String uuid, byte[] data) {
        synchronized (computers) {
            for (IComputerAccess computer : computers) {
                computer.queueEvent("mag_swipe", computer.getAttachmentName(), uuid, data);
            }
        }
    }

    public void magWrite(String uuid) {
        synchronized (computers) {
            for (IComputerAccess computer : computers) {
                computer.queueEvent("mag_write", computer.getAttachmentName(), uuid);
            }
        }
    }

    public void writeCard(byte[] data) {
        write = data;
        update();
    }

    public void cancelWrite() {
        write = null;
        update();
    }

    public void setState(BlockStateMagCardReader state) {
        this.state = state;
        update();
    }

    public BlockStateMagCardReader getState() {
        return state;
    }

    @Override
    public void update() {
        super.update();

        if(write == null) {
            this.getWorld().setBlockState(this.getPos(), this.getBlockState().with(BlockMagCardReader.STATE, state));
        } else {
            this.getWorld().setBlockState(this.getPos(), this.getBlockState().with(BlockMagCardReader.STATE, BlockStateMagCardReader.WRITE));
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack item = player.getHeldItem(hand);
        if (!item.isEmpty() && item.getItem().equals(ModItems.MAG_CARD)) {
            CompoundNBT tag = item.getOrCreateTag();
            if (!tag.contains("uuid")) {
                tag.putString("uuid", UUID.randomUUID().toString());
            }

            if (write != null) {
                tag.putByteArray("data", write);
                write = null;
                update();
                magWrite(tag.getString("uuid"));
            } else {
                if (tag.contains("data")) {
                    magSwipe(tag.getString("uuid"), tag.getByteArray("data"));
                } else {
                    magSwipe(tag.getString("uuid"), null);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }


}
package com.lmvictor20.glypher.screen.preview;

import com.lmvictor20.glypher.model.GlypherSession;
import com.lmvictor20.glypher.service.GlypherServices;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.client.gui.screen.ingame.SmokerScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public final class PreviewFactory {
    private PreviewFactory() {
    }

    public static Screen create(GlypherServices services, GlypherSession session, Screen backScreen) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory previewInventory = createPreviewPlayerInventory(client);
        Text blankTitle = Text.empty();

        return switch (session.menuKind()) {
            case PLAYER_INVENTORY -> new GlypherPlayerInventoryPreviewScreen(services, session, backScreen);
            case CHEST_9X1 -> new GlypherGenericContainerPreviewScreen(fillAllSlotsWithPaper(GenericContainerScreenHandler.createGeneric9x1(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case CHEST_9X2 -> new GlypherGenericContainerPreviewScreen(fillAllSlotsWithPaper(GenericContainerScreenHandler.createGeneric9x2(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case CHEST_9X3 -> new GlypherGenericContainerPreviewScreen(fillAllSlotsWithPaper(GenericContainerScreenHandler.createGeneric9x3(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case CHEST_9X6 -> new GlypherGenericContainerPreviewScreen(fillAllSlotsWithPaper(GenericContainerScreenHandler.createGeneric9x6(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case HOPPER -> new GlypherHopperPreviewScreen(fillAllSlotsWithPaper(new HopperScreenHandler(0, previewInventory, createFilledInventory(5))), previewInventory, blankTitle, services, session, backScreen);
            case DISPENSER, DROPPER -> new GlypherDispenserPreviewScreen(fillAllSlotsWithPaper(new Generic3x3ContainerScreenHandler(0, previewInventory, createFilledInventory(9))), previewInventory, blankTitle, services, session, backScreen);
            case FURNACE -> new GlypherFurnacePreviewScreen(fillAllSlotsWithPaper(new FurnaceScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case BLAST_FURNACE -> new GlypherBlastFurnacePreviewScreen(fillAllSlotsWithPaper(new BlastFurnaceScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case SMOKER -> new GlypherSmokerPreviewScreen(fillAllSlotsWithPaper(new SmokerScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case ANVIL -> new GlypherAnvilPreviewScreen(fillAllSlotsWithPaper(new AnvilScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case BEACON -> new GlypherBeaconPreviewScreen(fillAllSlotsWithPaper(new BeaconScreenHandler(0, createFilledInventory(1), new ArrayPropertyDelegate(3), ScreenHandlerContext.EMPTY)), previewInventory, blankTitle, services, session, backScreen);
            case BREWING_STAND -> new GlypherBrewingStandPreviewScreen(fillAllSlotsWithPaper(new BrewingStandScreenHandler(0, previewInventory, createFilledInventory(5), new ArrayPropertyDelegate(2))), previewInventory, blankTitle, services, session, backScreen);
            case ENCHANTMENT -> new GlypherEnchantmentPreviewScreen(fillAllSlotsWithPaper(new EnchantmentScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case CARTOGRAPHY -> new GlypherCartographyPreviewScreen(fillAllSlotsWithPaper(new CartographyTableScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case GRINDSTONE -> new GlypherGrindstonePreviewScreen(fillAllSlotsWithPaper(new GrindstoneScreenHandler(0, previewInventory, ScreenHandlerContext.EMPTY)), previewInventory, blankTitle, services, session, backScreen);
            case LOOM -> new GlypherLoomPreviewScreen(fillAllSlotsWithPaper(new LoomScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case SMITHING -> new GlypherSmithingPreviewScreen(fillAllSlotsWithPaper(new SmithingScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case STONECUTTER -> new GlypherStonecutterPreviewScreen(fillAllSlotsWithPaper(new StonecutterScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
            case CRAFTER -> new GlypherCrafterPreviewScreen(fillAllSlotsWithPaper(new CrafterScreenHandler(0, previewInventory)), previewInventory, blankTitle, services, session, backScreen);
        };
    }

    private static PlayerInventory createPreviewPlayerInventory(MinecraftClient client) {
        PlayerInventory previewInventory = new PlayerInventory(client.player);
        for (int slot = 0; slot < previewInventory.size(); slot++) {
            previewInventory.setStack(slot, new ItemStack(Items.PAPER));
        }
        return previewInventory;
    }

    private static SimpleInventory createFilledInventory(int size) {
        SimpleInventory inventory = new SimpleInventory(size);
        for (int slot = 0; slot < size; slot++) {
            inventory.setStack(slot, new ItemStack(Items.PAPER));
        }
        return inventory;
    }

    private static <T extends ScreenHandler> T fillAllSlotsWithPaper(T handler) {
        for (Slot slot : handler.slots) {
            slot.setStack(new ItemStack(Items.PAPER));
        }
        return handler;
    }
}

final class GlypherGenericContainerPreviewScreen extends GenericContainerScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherGenericContainerPreviewScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherHopperPreviewScreen extends HopperScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherHopperPreviewScreen(HopperScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherDispenserPreviewScreen extends Generic3x3ContainerScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherDispenserPreviewScreen(Generic3x3ContainerScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherFurnacePreviewScreen extends FurnaceScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherFurnacePreviewScreen(FurnaceScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherBlastFurnacePreviewScreen extends BlastFurnaceScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherBlastFurnacePreviewScreen(BlastFurnaceScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherSmokerPreviewScreen extends SmokerScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherSmokerPreviewScreen(SmokerScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherAnvilPreviewScreen extends AnvilScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherAnvilPreviewScreen(AnvilScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherBeaconPreviewScreen extends BeaconScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherBeaconPreviewScreen(BeaconScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherBrewingStandPreviewScreen extends BrewingStandScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherBrewingStandPreviewScreen(BrewingStandScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherEnchantmentPreviewScreen extends EnchantmentScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherEnchantmentPreviewScreen(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherCartographyPreviewScreen extends CartographyTableScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherCartographyPreviewScreen(CartographyTableScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherGrindstonePreviewScreen extends GrindstoneScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherGrindstonePreviewScreen(GrindstoneScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherLoomPreviewScreen extends LoomScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherLoomPreviewScreen(LoomScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherSmithingPreviewScreen extends SmithingScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherSmithingPreviewScreen(SmithingScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherStonecutterPreviewScreen extends StonecutterScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherStonecutterPreviewScreen(StonecutterScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherCrafterPreviewScreen extends CrafterScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherCrafterPreviewScreen(CrafterScreenHandler handler, PlayerInventory inventory, Text title, GlypherServices services, GlypherSession session, Screen backScreen) {
        super(handler, inventory, title);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

final class GlypherPlayerInventoryPreviewScreen extends InventoryScreen implements GlypherPreviewHost {
    private final GlypherPreviewController glypherController;

    GlypherPlayerInventoryPreviewScreen(GlypherServices services, GlypherSession session, Screen backScreen) {
        super(MinecraftClient.getInstance().player);
        this.glypherController = new GlypherPreviewController(services, session, backScreen);
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();
        this.glypherController.init(this, this::addDrawableChild);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    public GlypherPreviewController glypher$getController() {
        return this.glypherController;
    }

    @Override
    public int glypher$getTitleRenderX() {
        return this.x + this.titleX;
    }

    @Override
    public int glypher$getTitleRenderY() {
        return this.y + this.titleY;
    }
}

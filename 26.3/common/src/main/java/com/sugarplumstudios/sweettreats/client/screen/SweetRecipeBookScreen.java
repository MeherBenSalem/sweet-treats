package com.sugarplumstudios.sweettreats.client.screen;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.SweetTreatsConfig;
import com.sugarplumstudios.sweettreats.block.SliceableTreatBlock;
import com.sugarplumstudios.sweettreats.discovery.RecipeBookEntry;
import com.sugarplumstudios.sweettreats.item.TreatItem;
import com.sugarplumstudios.sweettreats.registry.ModItems;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The Sweet Treats recipe book. Left page: every recipe of the selected station as a grid (undiscovered ones are
 * shadowed). Right page: the selected recipe, its station, ingredients and food stats.
 */
public class SweetRecipeBookScreen extends Screen {
    private static final Identifier TEXTURE = SweetTreats.id("textures/gui/recipe_book.png");
    private static final int BOOK_W = 256;
    private static final int BOOK_H = 180;
    private static final int COLS = 5;
    private static final int ROWS = 6;
    private static final int PER_PAGE = COLS * ROWS;
    private static final int CELL = 20;
    private static final int GRID_X = 17;
    private static final int GRID_Y = 28;
    private static final int RIGHT_CX = 188;
    private static final int PAGE_TEXT_WIDTH = 100;
    private static final int TAB_W = 28;
    private static final int TEXT = 0xFF5A3324;
    private static final int TITLE = 0xFFC2577F;
    private static final int MUTED = 0xFF9C7A6B;

    private final Set<Identifier> discovered;
    private final boolean hideUndiscoveredIngredients;
    private final List<RecipeBookEntry> entries;
    private final Map<Category, List<RecipeBookEntry>> recipes = new EnumMap<>(Category.class);
    private Category category = Category.BAKING;
    private int page;
    @Nullable
    private RecipeBookEntry selected;
    private int left;
    private int top;
    private ItemStack hoveredStack = ItemStack.EMPTY;
    @Nullable
    private Component hoveredText;
    /** Undiscovered items drawn this frame ({x, y, size}), darkened afterwards on a layer above the items. */
    private final List<int[]> shadows = new ArrayList<>();

    public SweetRecipeBookScreen(Set<Identifier> discovered, boolean hideUndiscoveredIngredients, List<RecipeBookEntry> entries) {
        super(Component.translatable("gui.sweettreats.recipe_book.title"));
        this.discovered = discovered;
        this.hideUndiscoveredIngredients = hideUndiscoveredIngredients;
        this.entries = entries;
    }

    enum Category {
        BAKING(() -> new ItemStack(ModItems.BAKERY_OVEN.get())),
        MIXING(() -> new ItemStack(ModItems.MIXING_BOWL.get())),
        DECORATING(() -> new ItemStack(ModItems.DECORATION_STATION.get())),
        CRAFTING(() -> new ItemStack(Items.CRAFTING_TABLE));

        private final Supplier<ItemStack> icon;

        Category(Supplier<ItemStack> icon) {
            this.icon = icon;
        }

        Component title() {
            return Component.translatable("gui.sweettreats.recipe_book.category." + name().toLowerCase(Locale.ROOT));
        }
    }

    // ------------------------------------------------------------------ setup

    @Override
    protected void init() {
        left = (width - BOOK_W) / 2;
        top = (height - BOOK_H) / 2 + 10;
        loadRecipes();
        if (selected == null) {
            selectFirst();
        }
    }

    private void loadRecipes() {
        recipes.clear();
        for (Category tab : Category.values()) {
            recipes.put(tab, new ArrayList<>());
        }
        // The server sends the entries already sorted per station.
        for (RecipeBookEntry entry : entries) {
            recipes.get(Category.values()[entry.category().ordinal()]).add(entry);
        }
    }

    private List<RecipeBookEntry> current() {
        return recipes.getOrDefault(category, List.of());
    }

    private void selectFirst() {
        List<RecipeBookEntry> list = current();
        selected = list.isEmpty() ? null : list.get(0);
    }

    private int pageCount() {
        return Math.max(1, (current().size() + PER_PAGE - 1) / PER_PAGE);
    }

    private ItemStack result(RecipeBookEntry recipe) {
        return recipe.result();
    }

    private boolean isDiscovered(RecipeBookEntry recipe) {
        return discovered.contains(recipe.id());
    }

    // ------------------------------------------------------------------ rendering

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        hoveredStack = ItemStack.EMPTY;
        hoveredText = null;
        shadows.clear();
        // The dimmed world is drawn by extractBackground; there are no widgets.
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        renderTabs(g, mouseX, mouseY);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left, top, 0, 0, BOOK_W, BOOK_H, 256, 256);
        renderLeftPage(g, mouseX, mouseY);
        renderRightPage(g, mouseX, mouseY);
        drawShadows(g);

        if (!hoveredStack.isEmpty()) {
            g.setTooltipForNextFrame(font, hoveredStack, mouseX, mouseY);
        } else if (hoveredText != null) {
            g.setTooltipForNextFrame(font, hoveredText, mouseX, mouseY);
        }
    }

    private void renderTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            Category tab = categories[i];
            int x = tabX(i);
            boolean active = tab == category;
            int y = active ? top - 25 : top - 21;
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, active ? TAB_W : 0, 206, TAB_W, active ? 27 : 24, 256, 256);
            g.item(tab.icon.get(), x + 6, y + 5);
            if (isIn(mouseX, mouseY, x, top - 25, TAB_W, 25)) {
                hoveredText = tab.title();
            }
        }
    }

    private int tabX(int index) {
        return left + 14 + index * (TAB_W + 2);
    }

    private void renderLeftPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        List<RecipeBookEntry> list = current();
        g.text(font, category.title(), left + 16, top + 13, TITLE, false);
        long found = list.stream().filter(this::isDiscovered).count();
        String count = found + "/" + list.size();
        g.text(font, count, left + 120 - font.width(count), top + 13, MUTED, false);

        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
            RecipeBookEntry recipe = list.get(start + i);
            int x = left + GRID_X + (i % COLS) * CELL;
            int y = top + GRID_Y + (i / COLS) * CELL;
            boolean known = isDiscovered(recipe);
            boolean hovered = isIn(mouseX, mouseY, x, y, CELL, CELL);
            int u = recipe == selected ? 60 : hovered ? 20 : known ? 0 : 40;
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, 184, CELL, CELL, 256, 256);
            ItemStack stack = result(recipe);
            renderMaybeShadowed(g, stack, x + 2, y + 2, known);
            if (known) {
                g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 13, y + 1, 170, 184, 6, 6, 256, 256);
            }
            if (hovered) {
                if (known) {
                    hoveredStack = stack;
                } else {
                    hoveredText = Component.translatable("gui.sweettreats.recipe_book.undiscovered");
                }
            }
        }

        int arrowsY = top + 154;
        if (page > 0) {
            boolean hover = isIn(mouseX, mouseY, left + 18, arrowsY, 18, 12);
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left + 18, arrowsY, hover ? 98 : 80, 184, 18, 12, 256, 256);
        }
        if (page < pageCount() - 1) {
            boolean hover = isIn(mouseX, mouseY, left + 100, arrowsY, 18, 12);
            g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left + 100, arrowsY, hover ? 134 : 116, 184, 18, 12, 256, 256);
        }
        String pages = (page + 1) + " / " + pageCount();
        g.text(font, pages, left + 68 - font.width(pages) / 2, arrowsY + 2, MUTED, false);
    }

    private void renderRightPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int cx = left + RIGHT_CX;
        if (selected == null) {
            drawCenteredWrapped(g, Component.translatable("gui.sweettreats.recipe_book.empty"), cx, top + 70, 100, MUTED);
            return;
        }
        RecipeBookEntry recipe = selected;
        boolean known = isDiscovered(recipe);
        ItemStack result = result(recipe);

        // Name
        Component name = known ? result.getHoverName() : Component.translatable("gui.sweettreats.recipe_book.unknown_name");
        drawCenteredWrapped(g, name, cx, top + 12, 104, TITLE);

        // Big result icon
        g.pose().pushMatrix();
        g.pose().translate(cx - 16, top + 32);
        g.pose().scale(2.0F, 2.0F);
        g.item(result, 0, 0);
        g.pose().popMatrix();
        if (!known) {
            shadows.add(new int[]{cx - 16, top + 32, 32});
        }
        if (result.getCount() > 1) {
            String countText = "x" + result.getCount();
            g.text(font, countText, cx + 18, top + 54, TEXT, false);
        }
        if (known && isIn(mouseX, mouseY, cx - 16, top + 32, 32, 32)) {
            hoveredStack = result;
        }

        // Food stats
        int y = top + 68;
        if (known) {
            for (Component line : foodLines(result)) {
                drawCenteredFit(g, line, cx, y, PAGE_TEXT_WIDTH, TEXT);
                y += 10;
            }
        } else {
            drawCenteredWrapped(g, Component.translatable("gui.sweettreats.recipe_book.not_discovered"), cx, y, PAGE_TEXT_WIDTH, MUTED);
        }

        // Station
        int stationY = top + 92;
        ItemStack station = category.icon.get();
        Component stationText = stationText(recipe);
        int stationWidth = 18 + font.width(stationText);
        int sx = cx - stationWidth / 2;
        g.item(station, sx, stationY - 4);
        g.text(font, stationText, sx + 18, stationY, TEXT, false);

        // Ingredients
        if (!known && hideUndiscoveredIngredients) {
            drawCenteredWrapped(g, Component.translatable("gui.sweettreats.recipe_book.hidden_hint"), cx, top + 118, 100, MUTED);
            return;
        }
        renderIngredients(g, recipe, cx, top + 110, mouseX, mouseY);
    }

    private Component stationText(RecipeBookEntry recipe) {
        return switch (recipe.category()) {
            case BAKING -> Component.translatable("gui.sweettreats.recipe_book.station_timed",
                    Component.translatable("block.sweettreats.bakery_oven"), seconds(recipe.time()));
            case MIXING -> Component.translatable("gui.sweettreats.recipe_book.station_timed",
                    Component.translatable("block.sweettreats.mixing_bowl"), seconds(recipe.time()));
            case DECORATING -> Component.translatable("block.sweettreats.decoration_station");
            case CRAFTING -> Component.translatable("block.minecraft.crafting_table");
        };
    }

    private static String seconds(int ticks) {
        float seconds = ticks / 20.0F;
        return seconds == Math.floor(seconds) ? (int) seconds + "s" : String.format(Locale.ROOT, "%.1fs", seconds);
    }

    private List<Component> foodLines(ItemStack result) {
        List<Component> lines = new ArrayList<>();
        if (result.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SliceableTreatBlock treat) {
            lines.add(Component.translatable("gui.sweettreats.recipe_book.slices", treat.getSlices(), treat.getNutritionPerSlice()));
        } else {
            FoodProperties food = result.get(DataComponents.FOOD);
            if (food != null) {
                lines.add(Component.translatable("gui.sweettreats.recipe_book.food", food.nutrition(),
                        String.format(Locale.ROOT, "%.1f", food.saturation())));
            } else {
                lines.add(Component.translatable("gui.sweettreats.recipe_book.ingredient"));
            }
        }
        if (result.getItem() instanceof TreatItem treat && SweetTreatsConfig.foodEffects() && !treat.getTreatEffects().isEmpty()) {
            TreatItem.TreatEffect effect = treat.getTreatEffects().get(0);
            lines.add(TreatItem.describe(effect.createInstance(SweetTreatsConfig.effectDurationMultiplier())));
        }
        return lines;
    }

    private void renderIngredients(GuiGraphicsExtractor g, RecipeBookEntry recipe, int cx, int y, int mouseX, int mouseY) {
        List<List<ItemStack>> ingredients = recipe.ingredients();
        if (recipe.category() == RecipeBookEntry.Category.DECORATING && !ingredients.isEmpty()) {
            List<List<ItemStack>> toppings = ingredients.subList(1, ingredients.size());
            int total = 18 + 10 + toppings.size() * 18;
            int x = cx - total / 2;
            ingredientFrame(g, ingredients.get(0), x, y, mouseX, mouseY);
            g.text(font, "+", x + 20, y + 5, TEXT, false);
            for (int i = 0; i < toppings.size(); i++) {
                ingredientFrame(g, toppings.get(i), x + 28 + i * 18, y, mouseX, mouseY);
            }
            g.text(font, Component.translatable("gui.sweettreats.recipe_book.base_toppings"), cx - 52, y + 22, MUTED, false);
            return;
        }
        if (recipe.shaped()) {
            int w = recipe.width();
            int h = recipe.height();
            int x0 = cx - w * 9;
            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    int index = col + row * w;
                    ingredientFrame(g, index < ingredients.size() ? ingredients.get(index) : List.of(),
                            x0 + col * 18, y - 4 + row * 18, mouseX, mouseY);
                }
            }
            return;
        }
        int perRow = 5;
        for (int i = 0; i < ingredients.size(); i++) {
            int rowCount = Math.min(perRow, ingredients.size() - (i / perRow) * perRow);
            int x0 = cx - rowCount * 9;
            ingredientFrame(g, ingredients.get(i), x0 + (i % perRow) * 18, y + (i / perRow) * 18, mouseX, mouseY);
        }
        if (recipe.category() == RecipeBookEntry.Category.MIXING && !recipe.container().isEmpty()) {
            int rows = (ingredients.size() + perRow - 1) / perRow;
            int cy = y + rows * 18 + 6;
            Component label = Component.translatable("gui.sweettreats.recipe_book.container");
            int labelWidth = font.width(label) + 4 + 18;
            int lx = cx - labelWidth / 2;
            g.text(font, label, lx, cy + 5, MUTED, false);
            ingredientFrame(g, recipe.container(), lx + font.width(label) + 4, cy, mouseX, mouseY);
        }
    }

    private void ingredientFrame(GuiGraphicsExtractor g, List<ItemStack> options, int x, int y, int mouseX, int mouseY) {
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 152, 184, 18, 18, 256, 256);
        if (options.isEmpty()) {
            return;
        }
        ItemStack stack = options.get((int) ((Util.getMillis() / 1200L) % options.size()));
        g.item(stack, x + 1, y + 1);
        if (isIn(mouseX, mouseY, x, y, 18, 18)) {
            hoveredStack = stack;
        }
    }

    private void renderMaybeShadowed(GuiGraphicsExtractor g, ItemStack stack, int x, int y, boolean known) {
        g.item(stack, x, y);
        if (!known) {
            shadows.add(new int[]{x, y, 16});
        }
    }

    /**
     * Tinting an item while drawing it is gone since 1.21.6, so undiscovered items get a dark plum wash on top
     * instead of being drawn as a dark silhouette.
     */
    private void drawShadows(GuiGraphicsExtractor g) {
        if (shadows.isEmpty()) {
            return;
        }
        g.nextStratum();
        for (int[] shadow : shadows) {
            g.fill(shadow[0], shadow[1], shadow[0] + shadow[2], shadow[1] + shadow[2], 0xD8523345);
        }
    }

    /** Draws one centred line, shrinking it slightly if it would not fit on the page. */
    private void drawCenteredFit(GuiGraphicsExtractor g, Component text, int cx, int y, int maxWidth, int color) {
        int width = font.width(text);
        if (width <= maxWidth) {
            g.text(font, text, cx - width / 2, y, color, false);
            return;
        }
        float scale = maxWidth / (float) width;
        g.pose().pushMatrix();
        g.pose().translate(cx - maxWidth / 2.0F, y + (1.0F - scale) * 4.0F);
        g.pose().scale(scale, scale);
        g.text(font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    private void drawCenteredWrapped(GuiGraphicsExtractor g, Component text, int cx, int y, int width, int color) {
        List<FormattedCharSequence> lines = font.split(text, width);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            FormattedCharSequence line = lines.get(i);
            g.text(font, line, cx - font.width(line) / 2, y + i * 10, color, false);
        }
    }

    private static boolean isIn(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (MouseButtons.of(event) == MouseButtons.LEFT) {
            Category[] categories = Category.values();
            for (int i = 0; i < categories.length; i++) {
                if (isIn(mouseX, mouseY, tabX(i), top - 25, TAB_W, 25) && categories[i] != category) {
                    category = categories[i];
                    page = 0;
                    selectFirst();
                    playClick();
                    return true;
                }
            }
            List<RecipeBookEntry> list = current();
            int start = page * PER_PAGE;
            for (int i = 0; i < PER_PAGE && start + i < list.size(); i++) {
                int x = left + GRID_X + (i % COLS) * CELL;
                int y = top + GRID_Y + (i / COLS) * CELL;
                if (isIn(mouseX, mouseY, x, y, CELL, CELL)) {
                    selected = list.get(start + i);
                    playClick();
                    return true;
                }
            }
            int arrowsY = top + 154;
            if (page > 0 && isIn(mouseX, mouseY, left + 18, arrowsY, 18, 12)) {
                turnPage(-1);
                return true;
            }
            if (page < pageCount() - 1 && isIn(mouseX, mouseY, left + 100, arrowsY, 18, 12)) {
                turnPage(1);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        if (delta > 0 && page > 0) {
            turnPage(-1);
            return true;
        }
        if (delta < 0 && page < pageCount() - 1) {
            turnPage(1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, delta);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft != null && minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void turnPage(int direction) {
        page = Math.max(0, Math.min(pageCount() - 1, page + direction));
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

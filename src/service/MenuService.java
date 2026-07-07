package service;

import model.MenuItem;
import model.enums.MenuCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This service manages the menu catalog. It uses Java 8 Streams to quickly filter active items for the wait staff's POS screen.
 */
public class MenuService {
    private final List<MenuItem> menuCatalog;

    public MenuService() {
        this.menuCatalog = new ArrayList<>();
    }

    public void addMenuItem(MenuItem item){
        menuCatalog.add(item);
    }

    public Optional<MenuItem> getItemById(int id){
        return menuCatalog.stream()
                          .filter(item -> item.getId() ==id)
                          .findFirst();

    }

    public List<MenuItem> getAvailableItemsByCategory(MenuCategory category){
        return menuCatalog.stream()
                .filter(MenuItem::isAvailable)
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }

    public void setItemAvailability(int itemId,boolean available){
        getItemById(itemId).ifPresent(item->item.setAvailable(available));
    }

    public List<MenuItem> getMenuCatalog() {
        return this.menuCatalog;
    }

    public void setMenuCatalog(List<MenuItem> loadedMenu) {
        this.menuCatalog.clear();
        this.menuCatalog.addAll(loadedMenu);
    }
}

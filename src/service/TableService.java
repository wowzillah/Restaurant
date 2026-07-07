package service;

import model.Reservation;
import model.Table;
import model.enums.TableStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableService {
    private final List<Table> tables;

    public TableService(){
        this.tables = new ArrayList<>();
    }

    public void addTable(Table table){
        tables.add(table);
    }

    public Optional<Table> getTableByNumber(int tableNumber){
         return tables.stream()
                 .filter(t ->t.getTableNumber()==tableNumber)
                 .findFirst();
    }

    /**
     * fin the smallest available table that can fit the guest count
     * @param guestCount the number of guest in the reservation
     * @return the tables available
     */
    public Optional<Table> findAvailableTableForParty(int guestCount){
        return tables.stream()
                .filter(t-> t.getStatus() == TableStatus.AVAILABLE)
                .filter(t ->t.getCapacity()>=guestCount)
                //sort by capacity ascending so a party of 2 get a 2-top and not an 8 top
                .sorted((t1, t2) -> Integer.compare(t1.getCapacity(), t2.getCapacity()))
                .findFirst();
    }

    public void seatParty(int tableNumber, Reservation reservation){
        Table table = getTableByNumber(tableNumber).orElseThrow(() -> new IllegalArgumentException("Table #" + tableNumber + " does not exist."));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table #" + tableNumber + " is currently " + table.getStatus());
        }

        table.assignReservation(reservation);

    }

    public void setTables(List<Table> loadedTables) {
        this.tables.clear();
        this.tables.addAll(loadedTables);
    }

    public List<Table> getTables() {
        return this.tables;
    }
}

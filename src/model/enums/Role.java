package model.enums;
/**
 * Waiter : (Front-of-House):
 *
 * Needs to: View the floor plan, open/close tables, add menu items to active orders, and request bills.
 *
 * Should NOT: Edit menu prices, view daily revenue reports, or clear the kitchen queue.
 *
 * KITCHEN (Back-of-House / Chef):
 *
 * Needs to: View incoming order tickets on the Kitchen Display System (KDS)
 * and update item statuses (PENDING  IN_PREPARATION  READY_TO_SERVE).
 * Should NOT: Create new orders, process customer payments, or modify table arrangements.
 *
 * MANAGER (Admin):
 *
 * Needs to: Have full access to everything.
 * They can add/remove menu items, view financial reports, override locked tables,
 * and manage staff accounts.
 */
public enum Role {
    WAITER,
    KITCHEN,
    MANAGER
}

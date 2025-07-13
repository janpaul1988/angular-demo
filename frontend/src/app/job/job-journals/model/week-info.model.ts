export interface WeekInfo {
  weekNumber: number;     // Week number (1-53)
  year: number;           // Year (e.g., 2025)
  startDate: Date;        // First day of the week
  endDate: Date;          // Last day of the week
  isCurrent: boolean;     // Whether this is the current week
  isSelected?: boolean;   // Whether this week is selected in the UI
}

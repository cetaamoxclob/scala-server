package data;

public enum DataState {
    Inserted, Updated, Deleted, ChildUpdated;
    public String toString() {
        return super.toString().toUpperCase();
    }
}

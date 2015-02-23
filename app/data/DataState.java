package data;

public enum DataState {
    Done, Inserted, Updated, Deleted, ChildUpdated;
    public String toString() {
        return super.toString().toUpperCase();
    }

    public static DataState fromString(String name) {
        if (name == null)
            throw new NullPointerException("Name is null");

        final DataState[] values = DataState.values();
        for(int i=0; i < values.length; i++) {
            if (name.toUpperCase().equals(values[i].toString().toUpperCase())) {
                return values[i];
            }
        }
        throw new IllegalArgumentException(
                "No enum constant " + DataState.class.getCanonicalName() + "." + name);
    }


}

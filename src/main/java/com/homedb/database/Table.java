package com.homedb.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface Table<T> {
    public String getName();
    public int insert(T item);
    public int insert(Set<T> items);
    public T select(String itemID);
    public List<T> select(int limit, int offset, String sortBy);
}

package service;

import java.util.List;

public interface IService<T> {
    void createPst(T t); // Using PreparedStatement
    void delete(T t);
    void update(T t);

    List<T> readAll();
    T readById(int id);
}
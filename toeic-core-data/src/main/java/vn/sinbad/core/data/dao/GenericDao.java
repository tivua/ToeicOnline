package vn.sinbad.core.data.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericDao<ID extends Serializable, T> {
    List<T> findAll();
    T update(T entity);
    void save(T entity);
    T findById(ID id);

    /*
    tra ve mang object vi vua la list, vua la size
    property -> truong can lay
    value -> gia tri, co the la obj, co the la chuoi, co the la so
    sortExpression -> sap xep theo truong nao
    sortDirection -> sap xep theo chieu nao*/
    Object[] findByProperty(String property, Object value, String sortExpression, String sortDirection);

    // API delete
    Integer delete(List<ID> ids);   //ids la so nhieu



}


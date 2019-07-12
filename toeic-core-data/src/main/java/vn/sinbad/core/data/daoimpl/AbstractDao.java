package vn.sinbad.core.data.daoimpl;

import org.hibernate.*;
import vn.sinbad.core.common.constant.CoreConstant;
import vn.sinbad.core.common.utils.HibernateUtils;
import vn.sinbad.core.data.dao.GenericDao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class AbstractDao<ID extends Serializable, T> implements GenericDao<ID, T> {

    //2 - phuong thuc lay entity, tuc la lay ten class do, vi du : UserDao, dua vao tien to nay  de lay va ghep vao persistenceClass
    private Class<T> persistenceClass;

    //3 -  de persistenceClass co gia tri, ta tao constructor Abstractor() -> khoi tao gia tri ban dau cho bien,
    public AbstractDao() {
        // 4 - ParameterizedType se lay mang [ID, T], sau do se lay vi tri getActualTypeArguments,
        // la vi tri that -> vi tri thu 1 -> la T,
        this.persistenceClass = (Class<T>) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];

    }

    //5 - chuyen tu Class<T> lay duoc sang String
    //vi du "select * from user" -> user la string, nhung persistenceClass la Class<T>
    // 6 - ham lay ten class entity
    public String getPersistenceClassName() {
        return persistenceClass.getSimpleName();
    }

    // 1 - xay dung doi tuong session de su dung --> tu getSessionFactory()
    //qua bai 19 -> xoa ham nay , phai tao session trong moi ham
    /*protected Session getSession() {
        return HibernateUtils.getSessionFactory().openSession();
    }*/

    public List<T> findAll() {
        List<T> list = new ArrayList<T>();
        Transaction transaction = null;
        Session session = HibernateUtils.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            //HQL  --> da co ham getPersistenceClassName() lay ten class entity : User, Comment, Role,....
            StringBuilder sql = new StringBuilder("from ");
            //lay ten entity
            sql.append(this.getPersistenceClassName());
            Query query = session.createQuery(sql.toString());
            list = query.list();
            transaction.commit();

        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }

        return list;
    }

    public T update(T entity) {
        T result = null;
        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
            Object object =  session.merge(entity);
            result = (T) object;
            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }
        return result;
    }

    public void save(T entity) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try{
            session.persist(entity);
            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }

    }

    public T findById(ID id) {
        T result = null;
        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            result = (T) session.get(persistenceClass, id);
            if(result == null) {
                throw new ObjectNotFoundException("NOT FOUND " + id, null);
            }
            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();;
            throw e;
        }finally {
            session.close();
        }
        return result;
    }

    public Object[] findByProperty(String property, Object value, String sortExpression, String sortDirection) {
        List<T> list = new ArrayList<T>();

        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        Object totalItem = 0;

        try{

            // from getPersistenceClassName where property= :value order by sortExpression sortDirection
            //sortDirection
            //1:ASC
            //2:DESC
            //sortDirection.equals("DESC") ? "ASC" : "DESC"
            StringBuilder sql1 = new StringBuilder("from ");
            sql1.append(getPersistenceClassName());

            if(property != null && value != null) {
                sql1.append(" where ");
                sql1.append(property);
                sql1.append("= :value");
            }
            if(sortExpression != null && sortDirection != null) {
                sql1.append(" order by ");
                sql1.append(sortExpression);
                sql1.append(" ").append(sortDirection.equals(CoreConstant.SORT_ASC)?"asc":"desc");
            }
            Query query1 = session.createQuery(sql1.toString());
            if(value != null) {
                query1.setParameter("value",value);
            }
            list = query1.list();

            // select count(*) from getPersistenceClassName where property= :value
            StringBuilder sql2 = new StringBuilder("select count(*) from ");
            sql2.append(getPersistenceClassName());
            if(property != null && value != null) {
                sql2.append(" where ");
                sql2.append(property);
                sql2.append("= :value");
            }

            Query query2 = session.createQuery(sql2.toString());
            if(value != null) {
                query2.setParameter("value", value);
            }
            totalItem = query2.list().get(0);   // tra ve object totalItem co kich thuoc la bao nhieu



            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }


        return new Object[]{totalItem, list};
    }

    public Integer delete(List<ID> ids) {
        Integer count = 0;  // bien dem so lan xoa item, sau do dem so sanh voi list, neu bang thi xoa thanh cong
        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try{
            for (ID item: ids) {
                T t = (T) session.get(persistenceClass, item);  // ham get yeu cau truyen doi so la kieu class
                session.delete(t);
                count++;
            }
            transaction.commit();
        }catch (HibernateException e){
            transaction.rollback();
            throw e;
        }finally {
            session.close();
        }

        return count;
    }


}

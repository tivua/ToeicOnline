package vn.sinbad.core.common.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtils {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory(){
        try {
            //create sessionfactory from hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        }catch (Throwable e){
            System.out.println("Initial SessionFactory  Fail");
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }


}

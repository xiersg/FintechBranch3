package com.financialfinshieldguard.authenticationservice.constants;

public class UserContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(long id) {threadLocal.set(id);}

    public static Long getCurrentId() {return threadLocal.get();}

    public static void removeCurrentId() {threadLocal.remove();}
}

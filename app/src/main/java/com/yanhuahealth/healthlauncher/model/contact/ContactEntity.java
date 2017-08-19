package com.yanhuahealth.healthlauncher.model.contact;

/**
 * Created by Administrator on 2016/1/15.
 */
public class ContactEntity {
    private long id;
    private String name;
    private long num;
    public ContactEntity(){}
    public ContactEntity(long id,String name,long num) {
        super();
        this.id = id;
        this.name = name;
        this.num = num;
    }
    public String toString() {
        return "id="+id+",name="+name+",num="+num;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setNum(long num) {
        this.num = num;
    }
    public String getName() {
        return name;
    }
    public long getNum() {
        return num;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
}

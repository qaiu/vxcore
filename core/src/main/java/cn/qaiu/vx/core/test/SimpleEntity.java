package cn.qaiu.vx.core.test;

import cn.qaiu.vx.core.annotations.GenerateServiceGen;

@GenerateServiceGen(idType = Long.class, generateProxy = true)
public class SimpleEntity {
    private Long id;
    private String name;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

package com.ngstudio.friendstep.model.entity.step;

import com.alexutils.annotations.DbAnnotation;
import com.alexutils.annotations.DbMainAnnotation;


import java.io.Serializable;

@DbMainAnnotation(tableName = "ContactStep", keyFields = {"id"})
public class ContactStep implements Serializable {
    @DbAnnotation
    public long id;
    @DbAnnotation
    public String name;

    public String getName() {
        return this.name;
    }
    public long getId() {
        return this.id;
    }
}

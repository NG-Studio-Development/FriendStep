package com.ngstudio.friendstep.components.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.alexutils.dao.GenericDao;
import com.alexutils.helpers.SQLiteInterlockHelper;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.model.entity.Contact;
import com.ngstudio.friendstep.model.entity.Message;
import com.ngstudio.friendstep.model.entity.step.ContactStep;

public class DbHelper extends SQLiteInterlockHelper {

    private static final String DB_NAME = "whereAreYou.db";
    private static final int VERSION = 1;

    protected DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GenericDao.getGenericDaoInstance(Contact.class).createTableQuery());
        db.execSQL(GenericDao.getGenericDaoInstance(Message.class).createTableQuery());
        db.execSQL(GenericDao.getGenericDaoInstance(ContactStep.class).createTableQuery());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class SingletonHolder {
        public static final DbHelper HOLDER_INSTANCE = new DbHelper(WhereAreYouApplication.getInstance());
    }

    public static DbHelper getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

}

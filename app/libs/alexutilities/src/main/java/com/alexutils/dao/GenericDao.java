package com.alexutils.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.alexutils.annotations.DbAnnotation;
import com.alexutils.annotations.DbMainAnnotation;
import com.alexutils.helpers.SQLiteInterlockHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class   GenericDao<DbHelper extends SQLiteInterlockHelper, DbEntity> {

    public static final long FAILED = -1;
    public static final long UPDATED = -2;
    public static final long DELETED = -3;

    private Class<DbEntity> genericDaoClass;
    private HashMap<String, Field> annotatedFields;
    private DbHelper dbHelper;
    private String[] keyFields;

    private boolean autoincrement;

    private GenericDao(@NotNull DbHelper dbHelper, @NotNull Class<DbEntity> genericDaoClass) {
        this.dbHelper = dbHelper;
        this.genericDaoClass = genericDaoClass;
        this.annotatedFields = getAnnotatedFields(genericDaoClass);

        if (genericDaoClass.isAnnotationPresent(DbMainAnnotation.class)) {
            DbMainAnnotation annotation = genericDaoClass.getAnnotation(DbMainAnnotation.class);
            autoincrement = annotation.autoincrement();
            keyFields = annotation.keyFields();

            List<Field> keys = new ArrayList<>();
            if(keyFields.length > 0) {
                for (String fieldName : keyFields) {
                    Field keyField = annotatedFields.get(fieldName);
                    if(keyField == null) {
                        throw new Error("Class should contain field which marked as key.");
                    } else {
                        keys.add(keyField);
                    }
                }
            }

            if(autoincrement && (keys.size() != 1 || !keys.get(0).getType().equals(long.class))) {
                throw new Error("Just class with one long key field can use autoincrement!");
            }
        }

    }

    public static <DbHelper extends SQLiteInterlockHelper, DbEntity> void init(@NotNull DbHelper dbHelper, @NotNull Class<DbEntity> genericDaoClass) {
        DaoInstancesHolder.INSTANCES.put(genericDaoClass, new GenericDao<>(dbHelper, genericDaoClass));
    }

    private static class DaoInstancesHolder {
        public static final Map<Class, GenericDao> INSTANCES
                = Collections.synchronizedMap(new WeakHashMap<Class, GenericDao>());
    }

    public static synchronized <DbHelper extends SQLiteInterlockHelper, DbEntity>
    GenericDao<DbHelper, DbEntity> getGenericDaoInstance(@NotNull Class<DbEntity> genericDaoClass) {

        if (!DaoInstancesHolder.INSTANCES.containsKey(genericDaoClass))
            throw new Error("Call init first!!!");

        //noinspection unchecked
        return DaoInstancesHolder.INSTANCES.get(genericDaoClass);
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    @SuppressWarnings("unchecked")
    public ContentValues toCv(@NotNull DbEntity dbEntity) {
        ContentValues cv = new ContentValues();
        for (String fieldKey : annotatedFields.keySet()) {
            Field field = annotatedFields.get(fieldKey);

            if(Arrays.asList(keyFields).contains(field.getName()) && autoincrement)
                continue;

            try {
                DbAnnotation dbAnnotation = field.getAnnotation(DbAnnotation.class);
                if (dbAnnotation.mapFields().length == 0) {
                    checkValueAndPutIntoCV(cv, dbAnnotation, field, dbEntity);

                } else {
                    HashMap<String, String> fieldsMap = new HashMap<>();

                    Object object = getValueForField(genericDaoClass, dbEntity, field);

                    if (object.getClass().equals(HashMap.class))
                        fieldsMap = HashMap.class.cast(object);

                    if (!fieldsMap.isEmpty()) {
                        final boolean isEncrypted = dbAnnotation.isEncrypted();
                        Method encryptMethod = isEncrypted ? genericDaoClass.getDeclaredMethod("encryptString", String.class) : null;

                        for (String mapField : dbAnnotation.mapFields()) {
                            String value = fieldsMap.get(mapField);
                            if (value != null) {

                                if (isEncrypted) {
                                    assert encryptMethod != null;
                                    cv.put(mapField, (byte[]) encryptMethod.invoke(dbEntity, value));
                                } else {
                                    cv.put(mapField, value);
                                }
                            }
                        }
                    }
                }

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                e.printStackTrace();
            }

        }
        return cv;
    }

    @SuppressWarnings("unchecked")
    public DbEntity fromCursor(Cursor cursor) {
        if (cursor == null)
            return null;

        try {
            DbEntity object = genericDaoClass.newInstance();

            for (String fieldKey : annotatedFields.keySet()) {
                Field field = annotatedFields.get(fieldKey);

                try {
                    DbAnnotation dbAnnotation = field.getAnnotation(DbAnnotation.class);

                    if (dbAnnotation.mapFields().length == 0) {
                        String name = (TextUtils.isEmpty(dbAnnotation.name())) ? field.getName() : dbAnnotation.name();
                        int index = cursor.getColumnIndex(name);

                        if (cursor.isNull(index))
                            continue;

                        Type fieldType = field.getType();

                        if (!dbAnnotation.isEncrypted()) {
                            String cursorGetter = "getString";

                            if (fieldType.equals(long.class) || fieldType.equals(int.class) || fieldType.equals(short.class) || fieldType.equals(double.class)
                                    || fieldType.equals(float.class))
                                cursorGetter = "get" + fieldType.toString().substring(0, 1).toUpperCase() + fieldType.toString().substring(1);

                            else if (fieldType.equals(byte[].class))
                                cursorGetter = "getBlob";

                            else if (fieldType.equals(boolean.class))
                                cursorGetter = "getShort";

                            else if (fieldType.equals(byte.class))
                                cursorGetter = "getShort";

                            Method cursorGetMethod = Cursor.class.getMethod(cursorGetter, int.class);

                            if (fieldType.equals(boolean.class))
                                setValueForField(genericDaoClass, object, field, (Short) cursorGetMethod.invoke(cursor, index) == 1);

                            else if (fieldType.equals(byte.class))
                                setValueForField(genericDaoClass,object, field, ((Short) cursorGetMethod.invoke(cursor, index)).byteValue());

                            else if (fieldType.equals(String[].class))
                                setValueForField(genericDaoClass, object, field, ((String) cursorGetMethod.invoke(cursor, index)).split(","));

                            else
                                setValueForField(genericDaoClass, object, field, cursorGetMethod.invoke(cursor, index));

                        } else {
                            String cursorGetter = "getBlob";
                            Method cursorGetMethod = Cursor.class.getMethod(cursorGetter, int.class);
                            byte[] encryptedBytes = (byte[]) cursorGetMethod.invoke(cursor, index);

                            Method decryptMethod = genericDaoClass.getDeclaredMethod("decryptString", byte[].class);
                            setValueForField(genericDaoClass, object, field, decryptMethod.invoke(object, encryptedBytes));
                        }

                    } else {
                        HashMap<String, String> fieldsMap = new HashMap<>();

                        for (String mapField : dbAnnotation.mapFields()) {
                            int index = cursor.getColumnIndex(mapField);

                            if (!cursor.isNull(index)) {
                                if (!dbAnnotation.isEncrypted()) {
                                    String value = cursor.getString(index);
                                    fieldsMap.put(mapField, value);

                                } else {
                                    byte[] encryptedBytes = cursor.getBlob(index);
                                    Method decryptMethod = genericDaoClass.getDeclaredMethod("decryptString", byte[].class);
                                    fieldsMap.put(mapField, (String) decryptMethod.invoke(object, (Object) encryptedBytes));
                                }
                            }
                        }

                        setValueForField(genericDaoClass, object, field, fieldsMap);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return object;

        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String createTableQuery() {
        String createTableRequest = "create table if not exists %s (%s);";
        StringBuilder queryBuilder = new StringBuilder();

        for (String fieldKey : annotatedFields.keySet()) {
            Field field = annotatedFields.get(fieldKey);

            DbAnnotation dbAnnotation = field.getAnnotation(DbAnnotation.class);
            if (dbAnnotation.mapFields().length == 0) {
                String name = (TextUtils.isEmpty(dbAnnotation.name())) ? field.getName() : dbAnnotation.name();
                queryBuilder.append(name)
                        .append(" ")
                        .append(dbAnnotation.dbType());

                if(autoincrement && Arrays.asList(keyFields).contains(field.getName())) {
                    queryBuilder.append(" PRIMARY KEY AUTOINCREMENT");
                }
                queryBuilder.append(",");

            } else {
                for (String mapField : dbAnnotation.mapFields()) {
                    queryBuilder.append(mapField)
                            .append(" ")
                            .append("text")
                            .append(",");
                }
            }
        }


        if (queryBuilder.length() > 0)
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);

        String tableName = getTableName();
        String table = TextUtils.isEmpty(tableName) ? genericDaoClass.getSimpleName() : tableName;

        return String.format(createTableRequest, table, queryBuilder.toString());
    }

    public boolean isExists(DbEntity dbEntity) {
        if (dbEntity == null)
            return false;

        final SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseReadable();
        if (db == null)
            return false;

        try {

            if (keyFields != null && keyFields.length != 0) {
                String where = arrayToWhereString(keyFields);
                String[] values = new String[keyFields.length];

                int i = 0;
                for (String fieldName : keyFields) {
                    Field field = annotatedFields.get(fieldName);

                    if (field == null) {
                        throw new IllegalArgumentException("Key fields should be marked with DbAnnotation");
                    }

                    Object fieldValue = getValueForField(genericDaoClass, dbEntity, field);
                    values[i] = fieldValue == null ? "" : String.valueOf(fieldValue);
                    i++;

                }
                Cursor cursor = db.getDatabase().query(getTableName(), null, where, values, null, null, null, null);

                boolean answer = false;
                if (cursor != null) {
                    answer = cursor.moveToFirst();
                    cursor.close();
                }
                return answer;
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            db.release();
        }

        return false;
    }


    @SafeVarargs
    public final boolean save(DbEntity... entities) {
        return save(Arrays.asList(entities));
    }


    public boolean save(Collection<DbEntity> entities) {
        return save(entities, false);
    }

    /**
     * Save all entities into db and return true if success or save nothing and return false if failure.
     *
     * @param entities The list of entities to save into db
     * @return true if success or false if failure
     */
    public boolean save(Collection<DbEntity> entities, boolean isAllOrNon) {
        if (entities == null || entities.isEmpty())
            return false;

        final SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            if (isAllOrNon)
                db.getDatabase().beginTransaction();

            for (DbEntity entity : entities) {
                long id = save(entity);
                if (id == FAILED && isAllOrNon)
                    throw new DaoException("Failed to save object " + entity.toString());
            }

            if (isAllOrNon)
                db.getDatabase().setTransactionSuccessful();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isAllOrNon)
                db.getDatabase().endTransaction();
            db.release();
        }

        return false;
    }

    /**
     * Save entity into database if not exists or update it otherwise.
     *
     * @param dbEntity entity to save into db
     * @return id of saved object if save, state UPDATED if updated, state FAILURE if fault
     */
    public long save(DbEntity dbEntity) {
        if (dbEntity == null)
            return FAILED;


        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return FAILED;

        long id = FAILED;
        try {
            boolean isExists = isExists(dbEntity);

            if (!isExists) {
                id = db.getDatabase().insert(getTableName(), null, toCv(dbEntity));

                if(autoincrement) {
                    setValueForField(genericDaoClass,dbEntity,annotatedFields.get(keyFields[0]),id);
                }

            } else {
                if (keyFields != null) {
                    String where = arrayToWhereString(keyFields);
                    String[] values = new String[keyFields.length];

                    int i = 0;
                    for (String fieldName : keyFields) {
                        Field field = annotatedFields.get(fieldName);

                        if (field == null) {
                            throw new IllegalArgumentException("Key fields should be marked with DbAnnotation");

                        } else {
                            Object fieldValue = getValueForField(genericDaoClass, dbEntity, field);
                            values[i] = String.valueOf(fieldValue);
                            i++;
                        }
                    }

                    if (db.getDatabase().update(getTableName(), toCv(dbEntity), where, values) > 0)
                        id = UPDATED;
                }

            }

        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            db.release();
        }

        return id;
    }

    public boolean update(DbEntity dbEntity, String whereClause, String[] whereArgs) {
        if (dbEntity == null)
            return false;

        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            if (db.getDatabase().update(getTableName(), toCv(dbEntity), whereClause, whereArgs) > 0)
                return true;
        } finally {
            db.release();
        }

        return false;
    }

    public boolean delete(@NotNull String[] keyValues) {
        if (keyFields == null) {
            throw new IllegalArgumentException("You should mark some fields as key to use this method!");
        }

        if (keyValues.length != keyFields.length) {
            throw new IllegalArgumentException("keyValues should be same length ans keyFields");
        }

        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            String where = arrayToWhereString(keyFields);
            return db.getDatabase().delete(getTableName(), where, keyValues) != 0;

        } finally {
            db.release();
        }

    }

    public boolean delete(@NotNull String whereClause, String... whereArgs) {

        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            db.getDatabase().delete(getTableName(), whereClause, whereArgs);
            return true;
        } finally {
            db.release();
        }

    }

    public boolean delete(DbEntity dbEntity) {
        if (dbEntity == null)
            return false;

        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            if (keyFields != null) {
                String where = arrayToWhereString(keyFields);
                String[] values = new String[keyFields.length];

                int i = 0;
                for (String fieldName : keyFields) {
                    Field field = annotatedFields.get(fieldName);

                    if (field == null) {
                        throw new IllegalArgumentException("Key fields should be marked with DbAnnotation");

                    } else {
                        Object fieldValue = getValueForField(genericDaoClass, dbEntity, field);
                        values[i] = (String) fieldValue;
                        i++;
                    }
                }

                db.getDatabase().delete(getTableName(), where, values);
                return true;
            }

        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            db.release();
        }

        return false;
    }

    public boolean delete(Collection<DbEntity> dbEntities) {
        if (dbEntities == null)
            return false;

        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseWritable();
        if (db == null)
            return false;

        try {
            if (keyFields != null) {
                String where = arrayToInWhereString(keyFields);
                String[] values = new String[keyFields.length];

                int i = 0;
                for (String fieldName : keyFields) {
                    Field field = annotatedFields.get(fieldName);

                    if (field == null) {
                        throw new IllegalArgumentException("Key fields should be marked with DbAnnotation");

                    } else {
                        List<String> fieldValues = new ArrayList<>();

                        for (DbEntity entity : dbEntities) {
                            Object fieldValue = getValueForField(genericDaoClass, entity, field);
                            fieldValues.add(fieldValue.toString());
                        }

                        values[i] = arrayToQueryString(fieldValues.toArray(new String[fieldValues.size()]));
                        i++;
                    }
                }

                db.getDatabase().delete(getTableName(), where, values);
                return true;
            }

        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            db.release();
        }
        return false;
    }

    public static SelectionPair getSelectionPairFromHashMap(HashMap<String, String> queryMap) {
        if (queryMap == null || queryMap.isEmpty())
            return null;

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        Iterator<String> keysIterator = queryMap.keySet().iterator();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            String value = queryMap.get(key);

            if (value == null)
                continue;

            if (keysIterator.hasNext())
                selection.append(key).append("=? AND ");
            else
                selection.append(key).append("=?");

            selectionArgs.add(value);
        }

        return new SelectionPair(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]));
    }

    public int getCount(@NotNull String where, String... args) {
        final SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseReadable();
        if (db == null)
            return 0;

        try {
            Cursor cursor = db.getDatabase().rawQuery("select count(*) from " + getTableName() + " where " + where, args);

            cursor.moveToFirst();
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                return cursor.getInt(0);
            } else {
                return 0;
            }

        } finally {
            db.release();
        }

    }

    public List<DbEntity> getObjects(HashMap<String, String> queryMap) {
        if (queryMap == null)
            return null;

        SelectionPair selectionPair = getSelectionPairFromHashMap(queryMap);
        return getObjects(selectionPair.selection, selectionPair.selectionValues, null, null, null, null);
    }

    public List<DbEntity> getObjects() {
        return getObjects(null, null, null, null, null, null);
    }

    public List<DbEntity> getObjects(String where, String[] args, String groupBy, String having, String orderBy, String limit) {
        final SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseReadable();
        if (db == null)
            return null;

        try {
            ArrayList<DbEntity> objects = null;

            final String tableName = getTableName();
            if (tableName != null) {
                Cursor cursor = db.getDatabase().query(
                        tableName, null, where, args, groupBy, having, orderBy, limit);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        objects = new ArrayList<>();
                        do {
                            DbEntity entity = fromCursor(cursor);
                            objects.add(entity);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            }

            return objects;

        } finally {
            db.release();
        }
    }

    public DbEntity getObjectById(@NotNull String... keyValues) {
        SQLiteInterlockHelper.DatabaseHandler db = dbHelper.acquireLockedDatabaseReadable();
        if (db == null)
            return null;

        try {
            DbEntity entity = null;

            final String tableName = getTableName();
            if (tableName != null) {

                if (genericDaoClass.isAnnotationPresent(DbMainAnnotation.class)) {
                    DbMainAnnotation annotation = genericDaoClass.getAnnotation(DbMainAnnotation.class);

                    if (annotation.keyFields() != null) {

                        if (keyValues.length != annotation.keyFields().length)
                            throw new IllegalArgumentException("keyValues should be same length ans keyFields");

                        String where = arrayToWhereString(annotation.keyFields());
                        Cursor cursor = db.getDatabase().query(annotation.tableName(), null, where, keyValues, null, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                entity = fromCursor(cursor);
                            }
                            cursor.close();
                        }
                    }
                }
            }

            return entity;

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            db.release();
        }

        return null;
    }

    private static String getGetterName(@NotNull Field field) {
        if (!field.getType().equals(boolean.class))
            return "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        else {
            if (field.getName().startsWith("is"))
                return field.getName();
            else
                return "is" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        }
    }

    private static String getSetterName(@NotNull Field field) {
        if (field.getType().equals(boolean.class) && field.getName().startsWith("is"))
            return field.getName().replace("is", "set");
        else
            return "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

    }

    private static <T> Object getValueForField(@NotNull Class<T> genericDaoClass, @NotNull Object object, @NotNull Field field) throws InvocationTargetException, IllegalAccessException {
        Object value;
        Method getter = null;

        try {
            getter = genericDaoClass.getMethod(getGetterName(field));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if(getter != null && field.getType().equals(getter.getReturnType())) {
            value = getter.invoke(object);

        } else {
            if(!field.isAccessible())
                field.setAccessible(true);

            value = field.get(object);
        }

        return value;
    }

    private static <T> void setValueForField(@NotNull Class<T> genericDaoClass, @NotNull Object object, @NotNull Field field, Object value) throws InvocationTargetException, IllegalAccessException {
        Method fieldSetter = null;

        try {
            fieldSetter = genericDaoClass.getMethod(getSetterName(field), field.getType());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if(fieldSetter != null) {
            fieldSetter.invoke(object,value);

        } else {
            if(!field.isAccessible())
                field.setAccessible(true);

            field.set(object,value);
        }

    }

    private void checkValueAndPutIntoCV(@NotNull ContentValues cv, @NotNull DbAnnotation dbAnnotation, @NotNull Field field, Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        if (object == null)
            return;

        Object fieldValue = getValueForField(object.getClass(),object,field);

        if (fieldValue == null && keyFields != null && Arrays.asList(keyFields).contains(field.getName()))
            fieldValue = "";

        if (fieldValue == null)
            return;

        String name = (TextUtils.isEmpty(dbAnnotation.name())) ? field.getName() : dbAnnotation.name();

        if (!dbAnnotation.isEncrypted()) {
            if (fieldValue instanceof String)
                cv.put(name, (String) fieldValue);

            else if (fieldValue instanceof Long)
                cv.put(name, (Long) fieldValue);

            else if (fieldValue instanceof Integer)
                cv.put(name, (Integer) fieldValue);

            else if (fieldValue instanceof Short)
                cv.put(name, (Short) fieldValue);

            else if (fieldValue instanceof Byte)
                cv.put(name, (Byte) fieldValue);

            else if (fieldValue instanceof Double)
                cv.put(name, (Double) fieldValue);

            else if (fieldValue instanceof Float)
                cv.put(name, (Float) fieldValue);

            else if (fieldValue instanceof Boolean)
                cv.put(name, (Boolean) fieldValue);

            else if (fieldValue instanceof byte[])
                cv.put(name, (byte[]) fieldValue);

            else if (fieldValue instanceof String[])
                cv.put(name, arrayToString((String[]) fieldValue));

            else {
                if (!TextUtils.isEmpty(dbAnnotation.foreignKey())) {
                    Field field1 = fieldValue.getClass().getDeclaredField(dbAnnotation.foreignKey());
                    checkValueAndPutIntoCV(cv, dbAnnotation, field1, fieldValue);
                }
            }

        } else {
            if (fieldValue instanceof String) {
                Method encryptMethod = genericDaoClass.getDeclaredMethod("encryptString", String.class);
                cv.put(name, (byte[]) encryptMethod.invoke(null, fieldValue));
            }
        }
    }

    public String getTableName() {
        if (genericDaoClass.isAnnotationPresent(DbMainAnnotation.class)) {
            DbMainAnnotation annotation = genericDaoClass.getAnnotation(DbMainAnnotation.class);
            return annotation.tableName();
        } else {
            return null;
        }
    }

    public DbEntity fromMap(HashMap<String, String> map) {
        if (map == null)
            return null;
        DbEntity entity = null;
        try {
            entity = genericDaoClass.newInstance();
            for (String fieldKey : annotatedFields.keySet()) {
                Field field = annotatedFields.get(fieldKey);

                DbAnnotation dbAnnotation = field.getAnnotation(DbAnnotation.class);
                try {
                    if (!dbAnnotation.isEncrypted()) {

                        if (dbAnnotation.mapFields().length == 0) {
                            String value = map.get(field.getName());
                            if (value != null) {
                                setValueForField(genericDaoClass, entity, field, value);
                            }

                        } else {
                            HashMap<String, String> fieldsMap = new HashMap<>();

                            for (String mapField : dbAnnotation.mapFields()) {
                                String value = map.get(mapField);
                                fieldsMap.put(mapField, value);

                            }
                            setValueForField(genericDaoClass, entity, field, fieldsMap);
                        }
                    }

                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> toMap(DbEntity dbEntity) {
        if (dbEntity == null)
            return null;
        HashMap<String, String> map = new HashMap<>();
        for (String fieldKey : annotatedFields.keySet()) {
            Field field = annotatedFields.get(fieldKey);

            DbAnnotation dbAnnotation = field.getAnnotation(DbAnnotation.class);
            try {
                if (!dbAnnotation.isEncrypted()) {
                    if (dbAnnotation.mapFields().length == 0) {
                        Object fieldValue = getValueForField(genericDaoClass, dbEntity, field);
                        if (fieldValue != null)
                            map.put(field.getName(), (String) fieldValue);
                    } else {
                        HashMap<String, String> fieldsMap = new HashMap<>();
                        Object object = getValueForField(genericDaoClass, dbEntity, field);

                        if (object.getClass().equals(HashMap.class))
                            fieldsMap = HashMap.class.cast(object);

                        if (!fieldsMap.isEmpty()) {
                            for (String mapField : dbAnnotation.mapFields()) {
                                String value = fieldsMap.get(mapField);
                                if (value != null)
                                    map.put(mapField, value);
                            }
                        }
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }

    private static String arrayToQueryString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String s : array) {
            builder.append("'").append(s).append("'")
                    .append(",");
        }

        return builder.toString().substring(0, builder.length() - 1);
    }

    private static String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (String s : array) {
            builder.append(s).append("'");
        }

        return builder.toString().substring(0, builder.length() - 1);
    }

    private static String arrayToWhereString(@NotNull String[] array) {
        if (array.length == 0)
            return null;

        StringBuilder builder = new StringBuilder(array[0]).append("=?");
        for (int i = 1; i < array.length; i++) {
            builder.append(" AND ")
                    .append(array[i])
                    .append("=?");
        }

        return builder.toString();
    }

    private static String arrayToInWhereString(@NotNull String[] array) {
        if (array.length == 0)
            return null;

        StringBuilder builder = new StringBuilder(array[0]).append("IN (?)");
        for (int i = 1; i < array.length; i++) {
            builder.append(" AND ")
                    .append(array[i])
                    .append("IN (?)");
        }

        return builder.toString();
    }

    private static class SelectionPair {
        String selection;
        String[] selectionValues;

        public SelectionPair(String selection, String[] selectionValues) {
            this.selection = selection;
            this.selectionValues = selectionValues;
        }
    }

    private HashMap<String, Field> getAnnotatedFields(Class<DbEntity> genericDaoClass) {
        HashMap<String, Field> allFields = new HashMap<>();

        Class current = genericDaoClass;
        while (!current.equals(Object.class)) {
            Field[] fields = current.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(DbAnnotation.class))
                    allFields.put(field.getName(), field);
            }

            current = current.getSuperclass();
        }
        return allFields;
    }

    public static class DaoException extends Exception {
        public DaoException(String message) {
            super(message);
        }
    }

}

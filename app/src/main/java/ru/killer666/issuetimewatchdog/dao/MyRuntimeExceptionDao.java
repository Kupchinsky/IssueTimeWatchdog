package ru.killer666.issuetimewatchdog.dao;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.LruObjectCache;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.ReferenceObjectCache;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.ObjectFactory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface MyRuntimeExceptionDao<T, ID> {

    /**
     * Retrieves an object associated with a specific ID.
     *
     * @param id
     *            Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     * @throws SQLException
     *             on any SQL problems or if more than 1 item with the id are found in the database.
     */
    public T queryForId(ID id);

    /**
     * Query for and return the first item in the object table which matches the PreparedQuery. See
     * {@link #queryBuilder()} for more information. This can be used to return the object that matches a single unique
     * column. You should use {@link #queryForId(Object)} if you want to query for the id column.
     *
     * @param preparedQuery
     *            Query used to match the objects in the database.
     * @return The first object that matches the query.
     * @throws SQLException
     *             on any SQL problems.
     */
    public T queryForFirst(PreparedQuery<T> preparedQuery);

    /**
     * Query for all of the items in the object table. For medium sized or large tables, this may load a lot of objects
     * into memory so you should consider using the {@link #iterator()} method instead.
     *
     * @return A list of all of the objects in the table.
     * @throws SQLException
     *             on any SQL problems.
     */
    public List<T> queryForAll();

    /**
     * Query for the items in the object table that match a simple where with a single field = value type of WHERE
     * clause. This is a convenience method for calling queryBuilder().where().eq(fieldName, value).query().
     *
     * @return A list of the objects in the table that match the fieldName = value;
     * @throws SQLException
     *             on any SQL problems.
     */
    public List<T> queryForEq(String fieldName, Object value);

    /**
     * Query for the rows in the database that match the object passed in as an argument. Any fields in the matching
     * object that are not the default value (null, false, 0, 0.0, etc.) are used as the matching parameters with AND.
     * If you are worried about SQL quote escaping, you should use {@link #queryForMatchingArgs(Object)}.
     */
    public List<T> queryForMatching(T matchObj);

    /**
     * Same as {@link #queryForMatching(Object)} but this uses {@link SelectArg} and SQL ? arguments. This is slightly
     * more expensive but you don't have to worry about SQL quote escaping.
     */
    public List<T> queryForMatchingArgs(T matchObj);

    /**
     * Query for the rows in the database that matches all of the field to value entries from the map passed in. If you
     * are worried about SQL quote escaping, you should use {@link #queryForFieldValuesArgs(Map)}.
     */
    public List<T> queryForFieldValues(Map<String, Object> fieldValues);

    /**
     * Same as {@link #queryForFieldValues(Map)} but this uses {@link SelectArg} and SQL ? arguments. This is slightly
     * more expensive but you don't have to worry about SQL quote escaping.
     */
    public List<T> queryForFieldValuesArgs(Map<String, Object> fieldValues);

    /**
     * Query for a data item in the table that has the same ID as the data parameter.
     */
    public T queryForSameId(T data);

    /**
     * Create and return a new query builder object which allows you to build a custom SELECT statement. You call
     * methods on the builder to construct your statement and then call {@link QueryBuilder#prepare()} once you are
     * ready to build. This returns a {@link PreparedQuery} object which gets passed to {@link #query(PreparedQuery)} or
     * {@link #iterator(PreparedQuery)}.
     */
    public QueryBuilder<T, ID> queryBuilder();

    /**
     * Like {@link #queryBuilder()} but allows you to build an UPDATE statement. You can then call call
     * {@link UpdateBuilder#prepare()} and pass the returned {@link PreparedUpdate} to {@link #update(PreparedUpdate)}.
     */
    public UpdateBuilder<T, ID> updateBuilder();

    /**
     * Like {@link #queryBuilder()} but allows you to build an DELETE statement. You can then call call
     * {@link DeleteBuilder#prepare()} and pass the returned {@link PreparedDelete} to {@link #delete(PreparedDelete)}.
     */
    public DeleteBuilder<T, ID> deleteBuilder();

    /**
     * Query for the items in the object table which match the prepared query. See {@link #queryBuilder} for more
     * information.
     *
     * <p>
     * <b>NOTE:</b> For medium sized or large tables, this may load a lot of objects into memory so you should consider
     * using the {@link #iterator(PreparedQuery)} method instead.
     * </p>
     *
     * @param preparedQuery
     *            Query used to match the objects in the database.
     * @return A list of all of the objects in the table that match the query.
     * @throws SQLException
     *             on any SQL problems.
     */
    public List<T> query(PreparedQuery<T> preparedQuery);

    /**
     * Create a new row in the database from an object.
     *
     * @param data
     *            The data item that we are creating in the database.
     * @return The number of rows updated in the database. This should be 1.
     */
    public int create(T data);

    /**
     * This is a convenience method to creating a data item but only if the ID does not already exist in the table. This
     * extracts the ID from the data parameter, does a {@link #queryForId(Object)} on it, returning the data if it
     * exists. If it does not exist {@link #create(Object)} will be called with the parameter.
     *
     * @return Either the data parameter if it was inserted (now with the ID field set via the create method) or the
     *         data element that existed already in the database.
     */
    public T createIfNotExists(T data);

    /**
     * This is a convenience method for creating an item in the database if it does not exist. The id is extracted from
     * the data argument and a query-by-id is made on the database. If a row in the database with the same id exists
     * then all of the columns in the database will be updated from the fields in the data parameter. If the id is null
     * (or 0 or some other default value) or doesn't exist in the database then the object will be created in the
     * database. This also means that your data item <i>must</i> have an id field defined.
     *
     * @return Status object with the number of rows changed and whether an insert or update was performed.
     */
    public Dao.CreateOrUpdateStatus createOrUpdate(T data);

    /**
     * Store the fields from an object to the database. If you have made changes to an object, this is how you persist
     * those changes to the database. You cannot use this method to update the id field -- see {@link #updateId} .
     *
     * <p>
     * NOTE: This will not save changes made to foreign objects or to foreign collections.
     * </p>
     *
     * @param data
     *            The data item that we are updating in the database.
     * @return The number of rows updated in the database. This should be 1.
     * @throws SQLException
     *             on any SQL problems.
     * @throws IllegalArgumentException
     *             If there is only an ID field in the object. See the {@link #updateId} method.
     */
    public int update(T data);

    /**
     * Update an object in the database to change its id to the newId parameter. The data <i>must</i> have its current
     * id set. If the id field has already changed then it cannot be updated. After the id has been updated in the
     * database, the id field of the data object will also be changed.
     *
     * <p>
     * <b>NOTE:</b> Depending on the database type and the id type, you may be unable to change the id of the field.
     * </p>
     *
     * @param data
     *            The data item that we are updating in the database with the current id.
     * @param newId
     *            The <i>new</i> id that you want to update the data with.
     * @return The number of rows updated in the database. This should be 1.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int updateId(T data, ID newId);

    /**
     * Update all rows in the table according to the prepared statement argument. To use this, the {@link UpdateBuilder}
     * must have set-columns applied to it using the {@link UpdateBuilder#updateColumnValue(String, Object)} or
     * {@link UpdateBuilder#updateColumnExpression(String, String)} methods.
     *
     * @param preparedUpdate
     *            A prepared statement to match database rows to be deleted and define the columns to update.
     * @return The number of rows updated in the database.
     * @throws SQLException
     *             on any SQL problems.
     * @throws IllegalArgumentException
     *             If there is only an ID field in the object. See the {@link #updateId} method.
     */
    public int update(PreparedUpdate<T> preparedUpdate);

    /**
     * Does a query for the object's id and copies in each of the field values from the database to refresh the data
     * parameter. Any local object changes to persisted fields will be overwritten. If the database has been updated
     * this brings your local object up to date.
     *
     * @param data
     *            The data item that we are refreshing with fields from the database.
     * @return The number of rows found in the database that correspond to the data id. This should be 1.
     * @throws SQLException
     *             on any SQL problems or if the data item is not found in the table or if more than 1 item is found
     *             with data's id.
     */
    public int refresh(T data);

    /**
     * Delete an object from the database.
     *
     * @param data
     *            The data item that we are deleting from the database.
     * @return The number of rows updated in the database. This should be 1.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int delete(T data);

    /**
     * Delete an object from the database that has an id.
     *
     * @param id
     *            The id of the item that we are deleting from the database.
     * @return The number of rows updated in the database. This should be 1.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int deleteById(ID id);

    /**
     * Delete a collection of objects from the database using an IN SQL clause.
     *
     * @param datas
     *            A collection of data items to be deleted.
     * @return The number of rows updated in the database. This should be the size() of the collection.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int delete(Collection<T> datas);

    /**
     * Delete the objects that match the collection of ids from the database using an IN SQL clause.
     *
     * @param ids
     *            A collection of data ids to be deleted.
     * @return The number of rows updated in the database. This should be the size() of the collection.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int deleteIds(Collection<ID> ids);

    /**
     * Delete the objects that match the prepared statement argument.
     *
     * @param preparedDelete
     *            A prepared statement to match database rows to be deleted.
     * @return The number of rows updated in the database.
     * @throws SQLException
     *             on any SQL problems.
     */
    public int delete(PreparedDelete<T> preparedDelete);

    /**
     * This satisfies the {@link Iterable} interface for the class and allows you to iterate through the objects in the
     * table using SQL. You can use code similar to the following:
     *
     * <p>
     * <blockquote>
     *
     * <pre>
     * for (Account account : accountDao) { ... }
     * </pre>
     *
     * </blockquote>
     * </p>
     *
     * <p>
     * <b>WARNING</b>: because the {@link Iterator#hasNext()}, {@link Iterator#next()}, etc. methods can only throw
     * {@link RuntimeException}, the code has to wrap any {@link SQLException} with {@link IllegalStateException}. Make
     * sure to catch {@link IllegalStateException} and look for a {@link SQLException} cause.
     * </p>
     *
     * <p>
     * <b>WARNING</b>: The underlying results object will only be closed if you page all the way to the end of the
     * iterator using the for() loop or if you call {@link CloseableIterator#close()} directly. You can also call the
     * {@link #closeLastIterator()} if you are not iterating across this DAO in multiple threads.
     * </p>
     *
     * @return An iterator of the class that uses SQL to step across the database table.
     *
     * @throws IllegalStateException
     *             When it encounters a SQLException or in other cases.
     */
    public CloseableIterator<T> iterator();

    /**
     * Same as {@link #iterator()} but while specifying flags for the results. This is necessary with certain database
     * types.
     *
     * <p>
     * <b>WARNING:</b> Depending on the database type the underlying connection may never be freed -- even if you go all
     * of the way through the results. It is <i>strongly</i> recommended that you call the
     * {@link CloseableIterator#close()} method when you are done with the iterator.
     * </p>
     */
    public CloseableIterator<T> iterator(int resultFlags);

    /**
     * Same as {@link #iterator()} but with a prepared query parameter. See {@link #queryBuilder} for more information.
     * You use it like the following:
     *
     * <p>
     * <blockquote>
     *
     * <pre>
     * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
     * ... custom query builder methods
     * CloseableIterator&lt;Account&gt; iterator = partialDao.iterator(qb.prepare());
     * try {
     *     while (iterator.hasNext()) {
     *         Account account = iterator.next();
     *         ...
     *     }
     * } finish {
     *     iterator.close();
     * }
     * </pre>
     *
     * </blockquote>
     * </p>
     *
     * @param preparedQuery
     *            Query used to iterate across a sub-set of the items in the database.
     * @return An iterator for T.
     * @throws SQLException
     *             on any SQL problems.
     */
    public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery);

    /**
     * Same as {@link #iterator(PreparedQuery)} but while specifying flags for the results. This is necessary with
     * certain database types.
     */
    public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery, int resultFlags);

    /**
     * This makes a one time use iterable class that can be closed afterwards. The DAO itself is
     * {@link CloseableWrappedIterable} but multiple threads can each call this to get their own closeable iterable.
     * This allows you to do something like:
     *
     * <blockquote>
     *
     * <pre>
     * CloseableWrappedIterable<Foo> wrappedIterable = fooDao.getWrappedIterable();
     * try {
     *   for (Foo foo : wrappedIterable) {
     *       ...
     *   }
     * } finally {
     *   wrappedIterable.close();
     * }
     * </pre>
     *
     * </blockquote>
     */
    public CloseableWrappedIterable<T> getWrappedIterable();

    /**
     * Same as {@link #getWrappedIterable()} but with a prepared query parameter. See {@link #queryBuilder} or
     * {@link #iterator(PreparedQuery)} for more information.
     */
    public CloseableWrappedIterable<T> getWrappedIterable(PreparedQuery<T> preparedQuery);

    /**
     * This closes the last iterator returned by the {@link #iterator()} method.
     *
     * <p>
     * <b>NOTE:</b> This is not reentrant. If multiple threads are getting iterators from this DAO then you should use
     * the {@link #getWrappedIterable()} method to get a wrapped iterable for each thread instead.
     * </p>
     */
    public void closeLastIterator();

    /**
     * Similar to the {@link #iterator(PreparedQuery)} except it returns a GenericRawResults object associated with the
     * SQL select query argument. Although you should use the {@link #iterator()} for most queries, this method allows
     * you to do special queries that aren't supported otherwise. Like the above iterator methods, you must call close
     * on the returned RawResults object once you are done with it. The arguments are optional but can be set with
     * strings to expand ? type of SQL.
     *
     * <p>
     * You can use the {@link QueryBuilder#prepareStatementString()} method here if you want to build the query using
     * the structure of the QueryBuilder.
     * </p>
     *
     * <p>
     *
     * <pre>
     * QueryBuilder&lt;Account, Integer&gt; qb = accountDao.queryBuilder();
     * qb.where().ge(&quot;orderCount&quot;, 10);
     * results = accountDao.queryRaw(qb.prepareStatementString());
     * </pre>
     *
     * </p>
     *
     * If you want to use the QueryBuilder with arguments to the raw query then you should do something like:
     *
     * <p>
     *
     * <pre>
     * QueryBuilder&lt;Account, Integer&gt; qb = accountDao.queryBuilder();
     * // we specify a SelectArg here to generate a ? in the statement string below
     * qb.where().ge(&quot;orderCount&quot;, new SelectArg());
     * // the 10 at the end is an optional argument to fulfill the SelectArg above
     * results = accountDao.queryRaw(qb.prepareStatementString(), rawRowMapper, 10);
     * </pre>
     *
     * </p>
     *
     * <p>
     * <b>NOTE:</b> If you are using the {@link QueryBuilder#prepareStatementString()} to build your query, it may have
     * added the id column to the selected column list if the Dao object has an id you did not include it in the columns
     * you selected. So the results might have one more column than you are expecting.
     * </p>
     */
    public GenericRawResults<String[]> queryRaw(String query, String... arguments);

    /**
     * Similar to the {@link #queryRaw(String, String...)} but this iterator returns rows that you can map yourself. For
     * every result that is returned by the database, the {@link RawRowMapper#mapRow(String[], String[])} method is
     * called so you can convert the result columns into an object to be returned by the iterator. The arguments are
     * optional but can be set with strings to expand ? type of SQL. For a simple implementation of a raw row mapper,
     * see {@link #getRawRowMapper()}.
     */
    public <UO> GenericRawResults<UO> queryRaw(String query, RawRowMapper<UO> mapper, String... arguments)
           ;

    /**
     * Similar to the {@link #queryRaw(String, String...)} but instead of an array of String results being returned by
     * the iterator, this uses the column-types parameter to return an array of Objects instead. The arguments are
     * optional but can be set with strings to expand ? type of SQL.
     */
    public GenericRawResults<Object[]> queryRaw(String query, DataType[] columnTypes, String... arguments)
           ;

    /**
     * Perform a raw query that returns a single value (usually an aggregate function like MAX or COUNT). If the query
     * does not return a single long value then it will throw a SQLException.
     */
    public long queryRawValue(String query, String... arguments);

    /**
     * Run a raw execute SQL statement to the database. The arguments are optional but can be set with strings to expand
     * ? type of SQL. If you have no arguments, you may want to call {@link #executeRawNoArgs(String)}.
     *
     * @return number of rows affected.
     */
    public int executeRaw(String statement, String... arguments);

    /**
     * Run a raw execute SQL statement on the database without any arguments. This may use a different mechanism to
     * execute the query depending on the database backend.
     *
     * @return number of rows affected.
     */
    public int executeRawNoArgs(String statement);

    /**
     * Run a raw update SQL statement to the database. The statement must be an SQL INSERT, UPDATE or DELETE
     * statement.The arguments are optional but can be set with strings to expand ? type of SQL.
     *
     * @return number of rows affected.
     */
    public int updateRaw(String statement, String... arguments);

    /**
     * Call the call-able that will perform a number of batch tasks. This is for performance when you want to run a
     * number of database operations at once -- maybe loading data from a file. This will turn off what databases call
     * "auto-commit" mode, run the call-able, and then re-enable "auto-commit". If auto-commit is not supported then a
     * transaction will be used instead.
     *
     * <p>
     * <b>NOTE:</b> If neither auto-commit nor transactions are supported by the database type then this may just call
     * the callable. Also, "commit()" is <i>not</i> called on the connection at all. If "auto-commit" is disabled then
     * this will leave it off and nothing will have been persisted.
     * </p>
     */
    public <CT> CT callBatchTasks(Callable<CT> callable) throws Exception;

    /**
     * Return the string version of the object with each of the known field values shown. Useful for testing and
     * debugging.
     *
     * @param data
     *            The data item for which we are returning the toString information.
     */
    public String objectToString(T data);

    /**
     * Return true if the two arguments are equal. This checks each of the fields defined in the database to see if they
     * are equal. Useful for testing and debugging.
     *
     * @param data1
     *            One of the data items that we are checking for equality.
     * @param data2
     *            The other data item that we are checking for equality.
     */
    public boolean objectsEqual(T data1, T data2);

    /**
     * Returns the ID from the data argument passed in. This is used by some of the internal queries to be able to
     * search by id.
     */
    public ID extractId(T data);

    /**
     * Returns the class of the DAO. This is used by internal query operators.
     */
    public Class<T> getDataClass();

    /**
     * Returns the class of the DAO. This is used by internal query operators.
     */
    public FieldType findForeignFieldType(Class<?> clazz);

    /**
     * Returns true if we can call update on this class. This is used most likely by folks who are extending the base
     * dao classes.
     */
    public boolean isUpdatable();

    /**
     * Returns true if the table already exists otherwise false.
     */
    public boolean isTableExists();

    /**
     * Returns the number of rows in the table associated with the data class. Depending on the size of the table and
     * the database type, this may be expensive and take a while.
     */
    public long countOf();

    /**
     * Returns the number of rows in the table associated with the prepared query passed in. Depending on the size of
     * the table and the database type, this may be expensive and take a while.
     */
    public long countOf(PreparedQuery<T> preparedQuery);

    /**
     * Creates an empty collection and assigns it to the appropriate field in the parent object. This allows you to add
     * things to the collection from the start.
     *
     * For example let's say you have an Account which has the field:
     *
     * <pre>
     * &#064;ForeignCollectionField(columnName = &quot;orders&quot;)
     * Collection&lt;Order&gt; orders;
     * </pre>
     *
     * You would then call:
     *
     * <pre>
     * accoundDao.assignEmptyForeignCollection(account, &quot;orders&quot;);
     * // this would add it the collection and the internal DAO
     * account.orders.add(order1);
     * </pre>
     *
     * @param parent
     *            Parent object that will be associated with all items added to this collection if not already assigned.
     * @param fieldName
     *            parameter is the field name of the foreign collection field -- you might consider using the
     *            {@link ForeignCollectionField#columnName()} to set the name to a static name.
     */
    public void assignEmptyForeignCollection(T parent, String fieldName);

    /**
     * Like {@link #assignEmptyForeignCollection(Object, String)} but it returns the empty collection that you assign to
     * the appropriate field.
     *
     * <p>
     * <b>NOTE:</b> May be deprecated in the future.
     * </p>
     */
    public <FT> ForeignCollection<FT> getEmptyForeignCollection(String fieldName);

    /**
     * Call this with true to enable an object cache for the DAO. Set to false to disable any caching. It is (as of
     * 9/2011) one of the newer features of ORMLite. It keeps a {@link ReferenceObjectCache} of the objects (using
     * {@link WeakReference}) referenced by the DAO. No support for objects returned by the {@link #queryRaw} methods.
     *
     * @throws SQLException
     *             If the DAO's class does not have an id field which is required by the {@link ObjectCache}.
     */
    public void setObjectCache(boolean enabled);

    /**
     * Same as {@link #setObjectCache(boolean)} except you specify the actual cache instance to use for the DAO. This
     * allows you to use a {@link ReferenceObjectCache} with {@link SoftReference} setting, the {@link LruObjectCache},
     * or inject your own cache implementation. Call it with null to disable the cache.
     *
     * @throws SQLException
     *             If the DAO's class does not have an id field which is required by the {@link ObjectCache}.
     */
    public void setObjectCache(ObjectCache objectCache);

    /**
     * Returns the current object-cache being used by the DAO or null if none.
     */
    public ObjectCache getObjectCache();

    /**
     * Flush the object cache if it has been enabled. This will remove an objects that are in the cache to reclaim
     * memory. Any future queries will re-request them from the database.
     */
    public void clearObjectCache();

    /**
     * Return the latest row from the database results from a query to select * (star).
     */
    public T mapSelectStarRow(DatabaseResults results);

    /**
     * Return a row mapper that is suitable for mapping results from a query to select * (star).
     */
    public GenericRowMapper<T> getSelectStarRowMapper();

    /**
     * Return a row mapper that is suitable for use with {@link #queryRaw(String, RawRowMapper, String...)}. This is a
     * bit experimental at this time. It most likely will _not_ work with all databases since the string output for each
     * data type is hard to forecast. Please provide feedback.
     */
    public RawRowMapper<T> getRawRowMapper();

    /**
     * Returns true if an object exists that matches this ID otherwise false.
     */
    public boolean idExists(ID id);

    /**
     * <p>
     * <b>WARNING:</b> This method is for advanced users only. It is only to support the
     * {@link #setAutoCommit(DatabaseConnection, boolean)} and other methods below. Chances are you should be using the
     * {@link #callBatchTasks(Callable)} instead of this method unless you know what you are doing.
     * </p>
     *
     * <p>
     * This allocates a connection for this specific thread that will be used in all other DAO operations. The thread
     * <i>must</i> call {@link #endThreadConnection(DatabaseConnection)} once it is done with the connection. It is
     * highly recommended that a
     * <code>try { conn = dao.startThreadConnection(); ... } finally { dao.endThreadConnection(conn); }</code> type of
     * pattern be used here to ensure you do not leak connections.
     * </p>
     */
    public DatabaseConnection startThreadConnection();

    /**
     * <p>
     * <b>WARNING:</b> This method is for advanced users only. It is only to support the
     * {@link #setAutoCommit(DatabaseConnection, boolean)} and other methods below. Chances are you should be using the
     * {@link #callBatchTasks(Callable)} instead of this method unless you know what you are doing.
     * </p>
     *
     * <p>
     * This method is used to free the connection returned by the {@link #startThreadConnection()} above.
     * </p>
     *
     * @param connection
     *            Connection to be freed. If null then it will be a no-op.
     */
    public void endThreadConnection(DatabaseConnection connection);

    /**
     * @deprecated You should use the {@link #setAutoCommit(DatabaseConnection, boolean)} method instead.
     */
    @Deprecated
    public void setAutoCommit(boolean autoCommit);

    /**
     * Set auto-commit mode to be true or false on the connection returned by the {@link #startThreadConnection()}. This
     * may not be supported by all database types.
     *
     * <p>
     * <b>WARNING:</b> Chances are you should be using the {@link #callBatchTasks(Callable)} instead of this method
     * unless you know what you are doing.
     * </p>
     */
    public void setAutoCommit(DatabaseConnection connection, boolean autoCommit);

    /**
     * @deprecated You should use the {@link #isAutoCommit(DatabaseConnection)} method instead.
     */
    @Deprecated
    public boolean isAutoCommit();

    /**
     * Return true if the database connection returned by the {@link #startThreadConnection()} is in auto-commit mode
     * otherwise false. This may not be supported by all database types.
     */
    public boolean isAutoCommit(DatabaseConnection connection);

    /**
     * If you have previously set auto-commit to false using {@link #setAutoCommit(DatabaseConnection, boolean)} then
     * this will commit all changes to the database made from that point up to now on the connection returned by the
     * {@link #startThreadConnection()}. The changes will be written to the database and discarded. The connection will
     * continue to stay in the current auto-commit mode.
     *
     * <p>
     * <b>WARNING:</b> Chances are you should be using the {@link #callBatchTasks(Callable)} instead of this method
     * unless you know what you are doing.
     * </p>
     */
    public void commit(DatabaseConnection connection);

    /**
     * If you have previously set auto-commit to false using {@link #setAutoCommit(DatabaseConnection, boolean)} then
     * this will roll-back and flush all changes to the database made from that point up to now on the connection
     * returned by the {@link #startThreadConnection()} . None of those changes will be written to the database and are
     * discarded. The connection will continue to stay in the current auto-commit mode.
     *
     * <p>
     * <b>WARNING:</b> Chances are you should be using the {@link #callBatchTasks(Callable)} instead of this method
     * unless you know what you are doing.
     * </p>
     */
    public void rollBack(DatabaseConnection connection);

    /**
     * Return the associated ConnectionSource or null if none set on the DAO yet.
     */
    public ConnectionSource getConnectionSource();

    /**
     * Set an object factory so we can wire in controls over an object when it is constructed. Set to null to disable
     * the factory.
     */
    public void setObjectFactory(ObjectFactory<T> objectFactory);
}
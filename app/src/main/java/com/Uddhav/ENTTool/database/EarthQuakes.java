package com.Uddhav.ENTTool.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.Uddhav.ENTTool.utils.AppSettings;
import com.Uddhav.ENTTool.utils.Tools;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Uddhav Gautam on 7.3.2016. upgautam@ualr.edu
 */
@DatabaseTable(tableName = "EarthQuakes")
public class EarthQuakes implements Parcelable, Comparator<EarthQuakes> {

    public static final Creator<EarthQuakes> CREATOR = new Creator<EarthQuakes>() {
        @Override
        public EarthQuakes createFromParcel(Parcel in) {
            return new EarthQuakes(in);
        }

        @Override
        public EarthQuakes[] newArray(int size) {
            return new EarthQuakes[size];
        }
    };
    @DatabaseField(id = true)
    private Long DateMilis;
    @DatabaseField
    private String LocationName;
    @DatabaseField
    private double Latitude;
    @DatabaseField
    private double Longitude;
    @DatabaseField
    private float Magnitude;
    @DatabaseField
    private float Depth;
    @DatabaseField
    private int Source;
    @DatabaseField
    private int Day;
    @DatabaseField
    private int Month;

    public EarthQuakes() {

    }

    protected EarthQuakes(Parcel in) {
        LocationName = in.readString();
        Latitude = in.readDouble();
        Longitude = in.readDouble();
        Magnitude = in.readFloat();
        Depth = in.readFloat();
        Source = in.readInt();
        Day = in.readInt();
        Month = in.readInt();
    }

    public static Long backDate() {

        int value = AppSettings.getInstance().getTimeInterval();

        int goBack = 0;

        if (value == 0) {
            goBack = 1;
        } else if (value == 1) {
            goBack = 7;
        } else if (value == 2) {
            goBack = 30;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -goBack);
        return cal.getTimeInMillis();
    }

    public void Insert() {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(DateMilis);
        Day = cal.get(Calendar.DAY_OF_MONTH);
        Month = cal.get(Calendar.MONTH) + 1;

        try {
            Dao<EarthQuakes, Long> Missionsinsert = (DatabaseHelper.getDbHelper()).getEarthQuakesDataHelper();
            EarthQuakes existenceCheck = Missionsinsert.queryForId(this.DateMilis);

            if (existenceCheck != null) {
                Missionsinsert.update(this);
            } else {
                Missionsinsert.create(this);
            }

        } catch (SQLException e) {
            Tools.catchException(e);
        }
    }

    public List<EarthQuakes> GetAllData() {

        List<EarthQuakes> data = new ArrayList<>();

        try {

            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();

            int sortingType = AppSettings.getInstance().getSorting();
            Long backdate = backDate();

            if (AppSettings.getInstance().getSource() == 0) {
                qBuilder.where()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude()) //
                        .and()//
                        .gt("DateMilis", backdate);
            } else {
                qBuilder.where()//
                        .eq("Source", AppSettings.getInstance().getSource()) //
                        .and()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude()) //
                        .and()//
                        .gt("DateMilis", backdate);
            }

            if (sortingType == 0) {
                qBuilder.orderBy("DateMilis", true);
            } else if (sortingType == 1) {
                qBuilder.orderBy("DateMilis", false);
            } else if (sortingType == 2) {
                qBuilder.orderBy("Magnitude", true);
            } else if (sortingType == 3) {
                qBuilder.orderBy("Magnitude", false);
            }

            PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
            data = dao.query(pQuery);

        } catch (SQLException e) {
            Tools.catchException(e);
        }

        return data;
    }

    public Long GetLastEarthQuakeDate() {

        List<EarthQuakes> data = new ArrayList<>();

        try {

            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();

            qBuilder.orderBy("DateMilis", false);

            PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
            data = dao.query(pQuery);

        } catch (SQLException e) {
            Tools.catchException(e);
        }

        return data.get(0).getDateMilis();
    }

    public List<EarthQuakes> newEarthquakes() {

        List<EarthQuakes> data = new ArrayList<>();

        try {

            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();

            if (AppSettings.getInstance().getSource() == 0) {
                qBuilder.distinct().where()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude()) //
                        .and()//
                        .gt("DateMilis", new LastEarthquakeDate().GetLastEarthquakeMilisDate());
            } else {
                qBuilder.distinct().where()//
                        .eq("Source", AppSettings.getInstance().getSource()) //
                        .and()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude()) //
                        .and()//
                        .gt("DateMilis", new LastEarthquakeDate().GetLastEarthquakeMilisDate());
            }

            qBuilder.orderBy("DateMilis", false);

            PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
            data = dao.query(pQuery);

        } catch (SQLException e) {
            Tools.catchException(e);
        }

        return data;
    }

    public int GetRowCount() {
        int count = 0;

        try {
            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            count = (int) dao.countOf();
        } catch (Exception e) {
            Tools.catchException(e);
        }

        return count;
    }

    public void DeleteRow(int deleteId) {
        try {

            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            DeleteBuilder<EarthQuakes, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("DateMilis", deleteId);
            deleteBuilder.delete();
        } catch (Exception e) {
            Tools.catchException(e);
        }
    }

    public EarthQuakes getEarthquakesById(Long DateMilis) {

        List<EarthQuakes> eqList = new ArrayList<>();

        try {
            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();
            qBuilder.distinct().where().eq("DateMilis", DateMilis);
            PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
            eqList = dao.query(pQuery);

        } catch (Exception e) {
            Tools.catchException(e);
        }

        return eqList.get(0);
    }

    public List<EarthQuakes> getEarthquakesByDay(int day, int month) {

        List<EarthQuakes> data = new ArrayList<>();

        try {

            Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
            QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();

            int sortingType = AppSettings.getInstance().getSorting();

            if (AppSettings.getInstance().getSource() == 0) {
                qBuilder.where()//
                        .eq("Day", day).and().eq("Month", month) //
                        .and()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude());
            } else {
                qBuilder.where()//
                        .eq("Day", day).and().eq("Month", month) //
                        .and()//
                        .eq("Source", AppSettings.getInstance().getSource()) //
                        .and()//
                        .gt("Magnitude", AppSettings.getInstance().getMagnitude());
            }

            if (sortingType == 0) {
                qBuilder.orderBy("DateMilis", true);
            } else if (sortingType == 1) {
                qBuilder.orderBy("DateMilis", false);
            } else if (sortingType == 2) {
                qBuilder.orderBy("Magnitude", true);
            } else if (sortingType == 3) {
                qBuilder.orderBy("Magnitude", false);
            }

            PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
            data = dao.query(pQuery);

        } catch (SQLException e) {
            Tools.catchException(e);
        }

        // try {
        // Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
        // QueryBuilder<EarthQuakes, Long> qBuilder = dao.queryBuilder();
        // qBuilder.where().eq("Day", day).and().eq("Month", month);
        // PreparedQuery<EarthQuakes> pQuery = qBuilder.prepare();
        // data = dao.query(pQuery);
        //
        // }
        // catch (Exception e) {
        // Tools.catchException(e);
        // }

        return data;
    }

    public List<EarthQuakes> getColumn(String ColumnName) throws SQLException {
        Dao<EarthQuakes, Long> dao = DatabaseHelper.getDbHelper().getEarthQuakesDataHelper();
        List<EarthQuakes> results = dao.queryBuilder().distinct().selectColumns(ColumnName).query();
        return results;
    }

    public Long getDateMilis() {
        return DateMilis;
    }

    public void setDateMilis(Long dateMilis) {
        DateMilis = dateMilis;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public float getMagnitude() {
        return Magnitude;
    }

    public void setMagnitude(float magnitude) {
        Magnitude = magnitude;
    }

    public float getDepth() {
        return Depth;
    }

    public void setDepth(float depth) {
        Depth = depth;
    }

    public int getSource() {
        return Source;
    }

    public void setSource(int source) {
        Source = source;
    }

    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public int getMonth() {
        return Month;
    }

    public void setMonth(int month) {
        Month = month;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

        dest.writeString(LocationName);
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
        dest.writeFloat(Magnitude);
        dest.writeFloat(Depth);
        dest.writeInt(Source);
        dest.writeInt(Day);
        dest.writeInt(Month);
    }

    @Override
    public int compare(EarthQuakes lhs, EarthQuakes rhs) {
        return (int) (lhs.getDateMilis() - rhs.getDateMilis());
    }

}

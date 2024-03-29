package com.example.benchmark_multithread;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class mtQueries {

    JSONObject workloadJsonObject;
    Context context;
    Utils utils;
    //Double SELECT;
    //Double UPDATE;
    //Double INSERT;
    //Double DELETE;

    public mtQueries(Context inContext){
        utils = new Utils();
        workloadJsonObject = Utils.workloadJsonObject;
        context = inContext;
    }

    public int startQueries(){


///*
        //utils.putMarker("{\"EVENT\":\"TESTBENCHMARK\"}", "trace_marker");

        //utils.putMarker("START: App started\n", "trace_marker");

        //utils.putMarker("{\"EVENT\":\"SQL_START\"}", "trace_marker");
        int tester = sqlQueries();
        if(tester != 0){
            return 1;
        }
        //utils.putMarker("{\"EVENT\":\"SQL_END\"}", "trace_marker");

        ///utils.putMarker("END: app finished\n", "trace_marker");

//*/
/*

        int tester = bdbQueries();
        if (tester != 0){
            return 1;
        }



*/

        return 0;
    }

    private int sqlQueries(){

        Thread t = Thread.currentThread();
        long tid = t.getId();

        SQLiteDatabase db = context.openOrCreateDatabase("SQLBenchmark",0,null);
        int sqlException = 0;

        while (true){

            MainActivity.lock.lock();
            int size = MainActivity.queriesQueue.size();
            MainActivity.lock.unlock();
            if(size == 0){
                break;
            }

            MainActivity.lock.lock();
            String query = MainActivity.queriesQueue.remove();
            MainActivity.lock.unlock();

            try {
                if(query.contains("SELECT")){

                    //startSql = System.currentTimeMillis();
                    //memBeforeQuery = utils.memoryAvailable(context);
                    //String start = "{\"EVENT\":\""+ Long.toString(tid) + "_START\"}";
                    //utils.putMarker(start, "trace_marker");
                    String start = "{\"EVENT\":\""+ Long.toString(tid) + "_START\"}";
                    utils.putMarker(start, "trace_marker");
                    Cursor cursor = db.rawQuery(query,null);
                    //MainActivity.queriesQueue.add(query);
                    //String end = "{\"EVENT\":\""+ Long.toString(tid) + "_END\"}";
                    //utils.putMarker(end, "trace_marker");
                    //memAfterQuery = utils.memoryAvailable(context);
                    //endSql = System.currentTimeMillis();
                    if(cursor.moveToFirst()) {
                        int numColumns = cursor.getColumnCount();
                        do {
                            int j=0;
                            while (j< numColumns) {
                                j++;
                            }
                            //String temp = cursor.toString();
                            //}
                            //process cursor
                        } while(cursor.moveToNext());
                    }
                    cursor.close();
                    String end = "{\"EVENT\":\""+ Long.toString(tid) + "_END\"}";
                    utils.putMarker(end, "trace_marker");
                    // SELECT++;

                }
                else {
                    //startSql = System.currentTimeMillis();
                    //memBeforeQuery = utils.memoryAvailable(context);
                    String start = "{\"EVENT\":\""+ Long.toString(tid) + "_START\"}";
                    utils.putMarker(start, "trace_marker");
                    db.execSQL(query);
                    String end = "{\"EVENT\":\""+ Long.toString(tid) + "_END\"}";
                    utils.putMarker(end, "trace_marker");
                    //MainActivity.queriesQueue.add(query);
                    //memAfterQuery = utils.memoryAvailable(context);
                    //endSql = System.currentTimeMillis();
                    /*
                           if(query.contains("UPDATE")) {
                               UPDATE++;
                            }
                            if(query.contains("INSERT")){
                                INSERT++;
                            }
                            if(query.contains("DELETE")){
                                DELETE++;
                            }*/
                }


                int tester = utils.sleepThread(1);
                if(tester != 0){
                    return 1;
                }

            }
            catch (SQLiteException e){
                sqlException = 1;
                continue;
            }

        }


        //utils.putMarker("{\"EVENT\":\"SELECT_END\"}\n","trace_marker");

        db.close();
        return 0;
    }

    private int bdbQueries(){

        Connection con = utils.jdbcConnection("BDBBenchmark");
        Statement stmt;
        int sqlException = 0;

        try {
            JSONArray benchmarkArray = workloadJsonObject.getJSONArray("benchmark");
            for(int i = 0; i < benchmarkArray.length(); i ++){
                JSONObject operationJson = benchmarkArray.getJSONObject(i);
                Object operationObject = operationJson.get("op");
                String operation = operationObject.toString();
                switch (operation) {
                    case "query": {
                        //double startBdb;
                        //double endBdb;
                        long memBeforeQuery;
                        long memAfterQuery;
                        sqlException = 0;
                        Object queryObject = operationJson.get("sql");
                        String query = queryObject.toString();

                        try {

                            stmt = con.createStatement();
                            //startBdb = System.currentTimeMillis();

                            if(query.contains("UPDATE")){
                                memBeforeQuery = utils.memoryAvailable(context);
                                int tester = stmt.executeUpdate(query);
                                memAfterQuery = utils.memoryAvailable(context);
                                if(tester == 0 || tester < 0){
                                    stmt.close();
                                    //throw new SQLiteException(query);
                                }
                            }
                            else {
                                memBeforeQuery = utils.memoryAvailable(context);
                                Boolean test = stmt.execute(query);
                                memAfterQuery = utils.memoryAvailable(context);
                                if (!test){
                                    stmt.close();
                                    //throw new SQLiteException();
                                }
                                //memAfterQuery = utils.memoryAvailable(context);
                                //endBdb = System.currentTimeMillis();
                                stmt.close();


                            }
                            /*
                            double delta = endBdb - startBdb;
                            double elapsedSeconds = delta / 1000.00000;
                            File file = new File(context.getFilesDir().getPath() + "/testBDB");
                            FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_APPEND);
                            fos.write((elapsedSeconds + ": " + query + "\n").getBytes());
                            fos.close();
                             */

                            /*
                            File file2 = new File(context.getFilesDir().getPath() + "/MemoryBDB");
                            FileOutputStream fos2;
                            fos2 = context.openFileOutput(file2.getName(), Context.MODE_APPEND);
                            fos2.write(("B Available: " + memBeforeQuery + "\n").getBytes());
                            fos2.write(("B Available: " + memAfterQuery + '\n').getBytes());
                            fos2.close();
                            */
                            /*
                            File file = new File(context.getFilesDir().getPath() + "/testBDB");
                            FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_APPEND);
                            fos.write((query + "\n").getBytes());
                            fos.close();
                            */


                        }
                        catch (SQLiteException e){
                            sqlException = 1;
                            /*
                            File file = new File(context.getFilesDir().getPath() + "/failedtestH2");
                            FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_APPEND);
                            fos.write((e + "\n").getBytes());
                            fos.close();
                            */

                            continue;
                        } catch (SQLException e) {
                            sqlException = 1;

                            /*
                            File file = new File(context.getFilesDir().getPath() + "/failedtestH2");
                            FileOutputStream fos = context.openFileOutput(file.getName(), Context.MODE_APPEND);
                            fos.write((e + "\n").getBytes());
                            fos.close();
                            */

                            e.printStackTrace();
                            continue;
                        }
                        break;
                    }
                    case "break": {

                        if(sqlException == 0) {
                            Object breakObject = operationJson.get("delta");
                            int breakTime = Integer.parseInt(breakObject.toString());
                            int tester = utils.sleepThread(breakTime);
                            if(tester != 0){
                                return 1;
                            }

                        }
                        sqlException = 0;
                        break;
                    }
                    default:
                        con.close();
                        return 1;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();

            try {
                con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            return 1;
        } /* catch (FileNotFoundException e) {
            e.printStackTrace();

            try {
                con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            return 1;
        } catch (IOException e) {
            e.printStackTrace();

            try {
                con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            return 1;
        }*/ catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }

        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

}
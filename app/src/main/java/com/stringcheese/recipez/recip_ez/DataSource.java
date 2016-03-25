package com.stringcheese.recipez.recip_ez;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Kevin on 3/24/2016.
 */
public class DataSource {
    private SQLiteDatabase db;
    private myDBHandler dbHelper;
    long recipe_id;
    private String[] recipeColumns = { dbHelper.COLUMN_RECIPE_NAME, dbHelper.COLUMN_RECIPE_SERVINGS,
            dbHelper.COLUMN_RECIPE_DESCRIPTION, dbHelper.COLUMN_RECIPE_DIRECTIONS };
    //TODO: ingredients

    public DataSource(Context context) {
        dbHelper = new myDBHandler(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    //add an ingredient to ingredients table
    public void addIngredient(Ingredient i){
        ContentValues values = new ContentValues();
        values.put(dbHelper.COLUMN_INGREDIENT_NAME, i.get_ingredientname());
        db.insert(dbHelper.INGREDIENTS_TABLE, null, values);
    }

    //TODO: right now this just deletes everything
    public void deleteIngredient(){
        db.execSQL("DELETE FROM " + dbHelper.INGREDIENTS_TABLE  + ";");
    }

    //add a recipe with its ingredients
    public void addRecipe(Recipe r){
        ContentValues values = new ContentValues();

        values.put(dbHelper.COLUMN_RECIPE_NAME, r.get_recipename());
        values.put(dbHelper.COLUMN_RECIPE_SERVINGS, r.get_servings());
        values.put(dbHelper.COLUMN_RECIPE_DESCRIPTION, r.get_description());
        values.put(dbHelper.COLUMN_RECIPE_DIRECTIONS, r.get_directions());

        //clean input
        recipe_id = db.insert(dbHelper.RECIPES_TABLE, null, values); //this should return the autoincremented recipe id

        //String sql = "INSERT INTO " + "recipes" + " VALUES ( null, ?, 1, ?, ?)";
        //db.execSQL(sql,new String[]{r.get_recipename(), r.get_description(), r.get_directions()});
        //update the listview


}




    public ArrayList<Ingredient> ingredientsToList(){
        String buffer;
        ArrayList<Ingredient>ingredients = new ArrayList<Ingredient>();
        String query = "SELECT * FROM " + dbHelper.INGREDIENTS_TABLE + ";";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            Ingredient i = new Ingredient();
            i.set_ingredientname(c.getString((c.getColumnIndex((myDBHandler.COLUMN_INGREDIENT_NAME)))));
            c.moveToNext();
        }
        return ingredients;
    }

    public List<Recipe> getAllRecipes(){
        List<Recipe> recipes = new ArrayList<Recipe>();

        Cursor cursor = db.query(myDBHandler.RECIPES_TABLE, recipeColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Recipe r = new Recipe();
            //r.set_id(cursor.getLong(cursor.getColumnIndex(myDBHandler.COLUMN_ID)));
            r.set_recipename(cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_NAME)));
            Log.d("TAG", r.get_recipename());
            r.set_servings(cursor.getInt(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_SERVINGS)));
           // r.set_description(cursor.getString(3));
           // r.set_directions(cursor.getString(4));
            recipes.add(r);
            cursor.moveToNext();
        }
        cursor.close();
        return recipes;

    }

    public void addRecipeIngredients(ArrayList<Ingredient>ingredients){

        for(Ingredient i: ingredients){
            /*
            ContentValues contentValues = new ContentValues();
            contentValues.put(dbHelper.COLUMN_RECIPE_ID, recipe_id);
            contentValues.put(dbHelper.COLUMN_RECIPE_INGREDIENTS_AMOUNT, i.get_amount());
            db.insert(dbHelper.RECIPE_INGREDIENTS_TABLE, null, contentValues);
            */


            Log.d("ING", i.get_ingredientname());
            ContentValues values = new ContentValues();
            values.put(dbHelper.COLUMN_INGREDIENT_NAME, i.get_ingredientname());
            db.insert(dbHelper.INGREDIENTS_TABLE, null, values);


            
            //String sql = "INSERT INTO " + "recipe_ingredients" + " VALUES ( ?, null, ?, null)";
            //db.execSQL(sql,new String[]{Long.toString(recipe_id), Integer.toString(i.get_amount())});

        }
    }
}

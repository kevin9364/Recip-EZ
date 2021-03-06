package com.stringcheese.recipez.recip_ez;

import java.util.ArrayList;
import java.util.List;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Kevin on 3/24/2016.
 */
public class DataSource {
    private Recipe currentRecipe;
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
    public void showRecipe(Recipe r){
        currentRecipe = r;

    }

    public Recipe getRecipeDetails(String name){
        Recipe r = new Recipe();
        r.set_recipename(name);
        String query = "SELECT * FROM " + myDBHandler.RECIPES_TABLE + " WHERE " + myDBHandler.COLUMN_RECIPE_NAME + " = \"" + name + "\" ;" ;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {

                long _id = cursor.getLong(cursor.getColumnIndex(myDBHandler.COLUMN_ID));
                int _serving = cursor.getInt(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_SERVINGS));
                String _description = cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_DESCRIPTION));
                String _directions = cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_DIRECTIONS));
                String _image = cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_IMAGE));


                r.set_id(_id);
                r.set_servings(_serving);
                r.set_description(_description);
                r.set_directions(_directions);
                r.set_image(_image);
            }
        return r;
    }

    public List<Ingredient> getRecipeIngredients(String recipe_id){
        List<Ingredient> iv = new ArrayList<Ingredient>();
        String query = "SELECT * FROM " + dbHelper.RECIPE_INGREDIENTS_TABLE + " JOIN " + dbHelper.INGREDIENTS_TABLE + " ON " + dbHelper.RECIPE_INGREDIENTS_TABLE + "." + dbHelper.COLUMN_INGREDIENT_ID + " = " + dbHelper.INGREDIENTS_TABLE + "." + dbHelper.COLUMN_ID + " WHERE " + dbHelper.COLUMN_RECIPE_ID  + " = \"" + recipe_id + "\";";
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Ingredient i = new Ingredient();
            //r.set_recipename(cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_NAME)));
            i.set_ingredientname(cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_INGREDIENT_NAME)));
            i.set_amount(cursor.getFloat(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_INGREDIENTS_AMOUNT)));
            i.set_amount_modifier(cursor.getString(cursor.getColumnIndex(myDBHandler.COLUMN_RECIPE_INGREDIENTS_AMOUNT_MODIFIER)));

            iv.add(i);
            cursor.moveToNext();
        }
        cursor.close();
        return iv;
    }



    //TODO: right now this just deletes everything
    public void deleteIngredient(){
        db.execSQL("DELETE FROM " + dbHelper.INGREDIENTS_TABLE  + ";");
    }

    //add a recipe with its ingredients
    public long addRecipe(Recipe r){
        ContentValues values = new ContentValues();

        values.put(dbHelper.COLUMN_RECIPE_NAME, r.get_recipename());
        values.put(dbHelper.COLUMN_RECIPE_SERVINGS, r.get_servings());
        values.put(dbHelper.COLUMN_RECIPE_DESCRIPTION, r.get_description());
        values.put(dbHelper.COLUMN_RECIPE_DIRECTIONS, r.get_directions());


        values.put(dbHelper.COLUMN_RECIPE_IMAGE, r.get_image());
        //clean input
        recipe_id = db.insert(dbHelper.RECIPES_TABLE, null, values); //this should return the autoincremented recipe id

        //String sql = "INSERT INTO " + "recipes" + " VALUES ( null, ?, 1, ?, ?)";
        //db.execSQL(sql,new String[]{r.get_recipename(), r.get_description(), r.get_directions()});
        //update the listview

        return recipe_id;
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



    public void addRecipeIngredients(long recipe_id, ArrayList<Ingredient>ingredients){

        for(Ingredient i: ingredients){
            long ingredient_id;
            ContentValues values = new ContentValues();
            values.put(dbHelper.COLUMN_RECIPE_ID, recipe_id);

            //check to see if the ingredient exists in the ingredients table
            String sql = "SELECT * FROM " + dbHelper.INGREDIENTS_TABLE + " WHERE " + dbHelper.COLUMN_INGREDIENT_NAME + " ='" + i.get_ingredientname() + "' ;";
            Cursor cursor = db.rawQuery(sql, null);
            //get the id
            if(cursor!=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                ingredient_id = cursor.getLong(cursor.getColumnIndex("_id"));
            }
            //add to the table
            else{
                ContentValues ivalues = new ContentValues();
                ivalues.put(dbHelper.COLUMN_INGREDIENT_NAME, i.get_ingredientname());
                ingredient_id = db.insert(dbHelper.INGREDIENTS_TABLE, null, ivalues);
            }

            values.put(dbHelper.COLUMN_INGREDIENT_ID, ingredient_id);
            values.put(dbHelper.COLUMN_RECIPE_INGREDIENTS_AMOUNT, i.get_amount());
            values.put(dbHelper.COLUMN_RECIPE_INGREDIENTS_AMOUNT_MODIFIER, i.get_amount_modifier());
            db.insert(dbHelper.RECIPE_INGREDIENTS_TABLE, null, values);

        }
    }


    public List<Ingredient> getGrocery(List<String> mergedList){
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        List<Long> recipe_ids = new ArrayList<Long>();
        for(String recipe_name: mergedList){
            recipe_name = recipe_name.substring(1);
            String query = "SELECT * FROM recipes WHERE recipe_name = '" + recipe_name + "';";
            //String query = "SELECT * FROM recipes WHERE recipe_name = '" + "Test" + "';";

            Cursor c = db.rawQuery(query, null);
            c.moveToFirst();
            if(c.getCount() > 0) {
                long recipe_id = c.getLong(c.getColumnIndex(dbHelper.COLUMN_ID));
                Log.v("MEAL", "recipe_id="+Long.toString((recipe_id)));
                recipe_ids.add(recipe_id);
            }
            else{
                Log.v("MEAL", "something emessedfaslkj");
            }
        }
        //get the list of ingredient objects from the recipe ids
        for(Long id:recipe_ids){
            Log.v("MEAL", Long.toString(id));
            ingredients.addAll(getRecipeIngredients(Long.toString(id)));
        }
        for(Ingredient t:ingredients){
            Log.v("MEAL", t.get_ingredientname());
        }
        return ingredients;
    }
}

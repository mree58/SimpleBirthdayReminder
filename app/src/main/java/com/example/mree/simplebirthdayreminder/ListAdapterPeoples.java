package com.example.mree.simplebirthdayreminder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListAdapterPeoples extends ArrayAdapter<String>{
	
	
	private final Activity _context;
	private final String[] _name;
	private final String[] _surname;
	private final String[] _birthdate;
	private final String[] _age;
	private final String[] _daysleft;

	 
	public ListAdapterPeoples(Activity context, String[] name, String[] surname, String[] birthdate, String[] age,String[] daysleft) {
	super(context, R.layout.custom_list_layout,name);
	this._context = context;
	this._name = name;
	this._surname = surname;
	this._birthdate = birthdate;
	this._age = age;
	this._daysleft = daysleft;
	}
	 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	 
	LayoutInflater inflater = _context.getLayoutInflater();
	View rowView = inflater.inflate(R.layout.custom_list_layout, null, true);
	
	TextView txtTitle = (TextView) rowView.findViewById(R.id.item_name);
	TextView txtTitle2 = (TextView) rowView.findViewById(R.id.item_surname);
	TextView txtTitle3 = (TextView) rowView.findViewById(R.id.item_age);
	TextView txtTitle4 = (TextView) rowView.findViewById(R.id.item_birthdate);
	TextView txtTitle5 = (TextView) rowView.findViewById(R.id.item_daysleft);
	
	txtTitle.setText(_name[position]);
	txtTitle2.setText(_surname[position]);
	txtTitle3.setText(_birthdate[position]);
	txtTitle4.setText(_age[position]);
	txtTitle5.setText(_daysleft[position]);
	
	return rowView;
	}
	 

}

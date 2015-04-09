package com.ngstudio.friendstep.utils;


import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.ngstudio.friendstep.model.entity.Contact;
import com.ngstudio.friendstep.model.entity.NearbyContact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactsFilter {

    public static Filter createContainsFilter(final ArrayAdapter adapter, final List originalList) {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                List<Contact> arrayList = (List<Contact>) results.values;
                adapter.clear();
                Iterator<Contact> iterator = arrayList.iterator();
                while (iterator.hasNext())
                    adapter.add(iterator.next());
                adapter.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Contact> FilteredArrList = new ArrayList<Contact>();

                if (constraint == null || constraint.length() == 0) {

                    results.count = originalList.size();
                    results.values = originalList;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    Iterator iterator = originalList.iterator();
                    Contact contact;

                    while (iterator.hasNext()) {
                        contact = (Contact)iterator.next();
                        if (contact.getContactname().toLowerCase().contains(constraint))
                            FilteredArrList.add(contact);
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
               }
                return results;
            }
        };
    }

    public static Filter createNearbyFilter(final ArrayAdapter adapter, final List nearbyList, final List contactsList) {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                List<Contact> arrayList = (List<Contact>) results.values;
                adapter.clear();
                Iterator<Contact> iterator = arrayList.iterator();
                while (iterator.hasNext())
                    adapter.add(iterator.next());
                adapter.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Contact> filteredArrList = new ArrayList<Contact>();

                    Iterator iteratorContacts = contactsList.iterator();
                    Iterator iteratorNearby = nearbyList.iterator();
                    Contact contact;
                    NearbyContact nearbyContact;

                    while (iteratorContacts.hasNext()) {
                        contact = (Contact)iteratorContacts.next();
                        while (iteratorNearby.hasNext()) {
                            nearbyContact = (NearbyContact)iteratorNearby.next();
                            if(contact.getMobilenumber().equals(nearbyContact.getMobilenumber()))
                                filteredArrList.add(contact);
                        }
                    }
                    results.count = filteredArrList.size();
                    results.values = filteredArrList;
                return results;
            }
        };
    }
}

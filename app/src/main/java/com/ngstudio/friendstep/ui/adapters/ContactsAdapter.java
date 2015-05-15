    package com.ngstudio.friendstep.ui.adapters;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Filter;
    import android.widget.Filterable;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.ngstudio.friendstep.R;
    import com.ngstudio.friendstep.WhereAreYouApplication;
    import com.ngstudio.friendstep.components.cache.AvatarBase64ImageDownloader;
    import com.ngstudio.friendstep.model.entity.Contact;
    import com.ngstudio.friendstep.model.entity.NearbyContact;
    import com.ngstudio.friendstep.model.entity.step.ContactStep;

    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.Iterator;
    import java.util.List;

public class ContactsAdapter extends BaseArrayAdapter<ContactStep> implements Filterable{

    private List<ContactStep> originalList;
    private ContactsFilter filter;

    public ContactsAdapter(Context context, int style, List<ContactStep> list) {
        super(context, style, list);
        this.originalList = new ArrayList<>(list);
        filter = new ContactsFilter();
    }


    @Override
    public void addAll(Collection collection) {
        super.addAll(collection,true);
        originalList.addAll(collection);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       ContactsHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_contacts, parent, false);
            holder = holderInitialise(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ContactsHolder)convertView.getTag();
        }

        ContactStep contact = getItem(position);

        WhereAreYouApplication.getInstance().getAvatarCache().displayImage(AvatarBase64ImageDownloader.getImageUriFor(contact.getName()),holder.ivIcon);
        holder.tvName.setText(contact.getName());
        //holder.tvMobile.setText(contact.getMobilenumber());
        return convertView;
    }

    private ContactsHolder holderInitialise(View view) {
        ContactsHolder holder = new ContactsHolder();
        holder.ivIcon = (ImageView) view.findViewById(R.id.ivContactsPhoto);
        holder.tvName = (TextView) view.findViewById(R.id.tvContactsName);
        holder.tvMobile = (TextView) view.findViewById(R.id.tvContactsMobile);
        return holder;
    }

    static class ContactsHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvMobile;
    }

    @Override
    public Filter getFilter() {
        //return ContactsFilter.createContains(this, originalList);
        //return ContactsFilter.createContainsFilter(this,originalList);
        return filter;
    }

    /*Filter filter = new Filter() {

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
            List<Contact> arrayList = (List<Contact>) results.values;
            clear();
            Iterator<Contact> iterator = arrayList.iterator();
            while (iterator.hasNext())
                add(iterator.next());
            notifyDataSetChanged();
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
                        if (contact.getContactname().contains(constraint))
                            FilteredArrList.add(contact);
                    }

                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
            results.values = FilteredArrList;
            return results;
        }
    };*/

    public void selectFilter(int SELECT_FILTER) {
        ((ContactsFilter)getFilter()).selectFilter(SELECT_FILTER);
    }

    public void setCoincidenceList(List coincidenceList) {
        filter.setCoincidenceList(coincidenceList);
    }

    public class ContactsFilter extends Filter {

        public final static int CONTAINS_FILTER = 0;
        public final static int COINCIDENCE_FILTER = 1;
        private int SELECT_FILTER;
        private List coincidenceList = new ArrayList();

        public void setCoincidenceList(List coincidenceList) {
            this.coincidenceList = coincidenceList;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
            List<ContactStep> arrayList = (List<ContactStep>) results.values;
            clear();
            Iterator<ContactStep> iterator = arrayList.iterator();
            while (iterator.hasNext())
                add(iterator.next());
            notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            if (SELECT_FILTER == COINCIDENCE_FILTER)
                return coincidenceInListData(constraint);
            else
                return contains(constraint);
        }


        protected FilterResults contains(CharSequence constraint) {
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


        protected FilterResults coincidenceInListData(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Contact> filteredArrList = new ArrayList<Contact>();

            Iterator iteratorContacts = originalList.iterator();
            Iterator iteratorNearby = coincidenceList.iterator();
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


        public void selectFilter(int SELECT_FILTER) {
            this.SELECT_FILTER = SELECT_FILTER;
        }
    }

}

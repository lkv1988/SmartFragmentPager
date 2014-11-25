package com.github.airk.smartfragmentpager;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {
    ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.view_pager);
        mPager.setOffscreenPageLimit(1);
        mPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return CursorLoaderListFragment.newInstance(i);
        }

        @Override
        public int getCount() {
            return 10;
        }
    }

    public static class CursorLoaderListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            Log.e("Fragment", "Number: " + number + " setUserVisibleHint " + isVisibleToUser);
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            if (isVisibleToUser && isResumed()) {
                getLoaderManager().initLoader(0, null, this);
            }
        }

        int number;

        public static CursorLoaderListFragment newInstance(int position) {
            CursorLoaderListFragment fragment = new CursorLoaderListFragment();
            Bundle args = new Bundle();
            args.putInt("data", position);
            fragment.setArguments(args);
            return fragment;
        }
        // This is the Adapter being used to display the list's data.
        SimpleCursorAdapter mAdapter;

        // If non-null, this is the current filter the user has provided.
        String mCurFilter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            number = getArguments().getInt("data");
        }

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            Log.e("Fragment", "onActivityCreated" + " " + number);
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No phone numbers");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, null,
                    new String[] { Contacts.People.DISPLAY_NAME },
                    new int[] { android.R.id.text1}, 0);
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            if (number == 0) {
                getLoaderManager().initLoader(0, null, this);
            }
        }

        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS
                    | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            final View searchView = SearchViewCompat.newSearchView(getActivity());
            if (searchView != null) {
                SearchViewCompat.setOnQueryTextListener(searchView,
                        new SearchViewCompat.OnQueryTextListenerCompat() {
                            @Override
                            public boolean onQueryTextChange(String newText) {
                                // Called when the action bar search text has changed.  Update
                                // the search filter, and restart the loader to do a new query
                                // with this filter.
                                String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                                // Don't do anything if the filter hasn't actually changed.
                                // Prevents restarting the loader when restoring state.
                                if (mCurFilter == null && newFilter == null) {
                                    return true;
                                }
                                if (mCurFilter != null && mCurFilter.equals(newFilter)) {
                                    return true;
                                }
                                mCurFilter = newFilter;
                                getLoaderManager().restartLoader(0, null, CursorLoaderListFragment.this);
                                return true;
                            }
                        });
                SearchViewCompat.setOnCloseListener(searchView,
                        new SearchViewCompat.OnCloseListenerCompat() {
                            @Override
                            public boolean onClose() {
                                if (!TextUtils.isEmpty(SearchViewCompat.getQuery(searchView))) {
                                    SearchViewCompat.setQuery(searchView, null, true);
                                }
                                return true;
                            }

                        });
                MenuItemCompat.setActionView(item, searchView);
            }
        }

        @Override public void onListItemClick(ListView l, View v, int position, long id) {
            // Insert desired behavior here.
            Log.i("FragmentComplexList", "Item clicked: " + id);
        }

        // These are the Contacts rows that we will retrieve.
        static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
                Contacts.People._ID,
                Contacts.People.DISPLAY_NAME,
        };

        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            setListShown(false);
            Log.e("Fragment", "onCreateLoader " + number);
            // This is called when a new Loader needs to be created.  This
            // sample only has one Loader, so we don't care about the ID.
            // First, pick the base URI to use depending on whether we are
            // currently filtering.
            Uri baseUri;
            if (mCurFilter != null) {
                baseUri = Uri.withAppendedPath(Contacts.People.CONTENT_FILTER_URI, Uri.encode(mCurFilter));
            } else {
                baseUri = Contacts.People.CONTENT_URI;
            }

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            String select = "((" + Contacts.People.DISPLAY_NAME + " NOTNULL) AND ("
                    + Contacts.People.DISPLAY_NAME + " != '' ))";
            return new CursorLoader(getActivity(), baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.People.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            // This is called when the last Cursor provided to onLoadFinished()
            // above is about to be closed.  We need to make sure we are no
            // longer using it.
            mAdapter.swapCursor(null);
        }
    }

}

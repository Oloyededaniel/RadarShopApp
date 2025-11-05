package com.radar.radarshop;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to handle Google Places Autocomplete with inline dropdown suggestions.
 * This class provides inline autocomplete functionality without opening a new activity.
 */
public class AddressAutocompleteHelper {
    
    private Activity activity;
    private EditText addressEditText;
    private EditText cityEditText;
    private EditText stateEditText;
    private EditText zipCodeEditText;
    private Spinner countrySpinner;
    private PlacesClient placesClient;
    private PopupWindow suggestionPopup;
    private ListView suggestionListView;
    private AutocompleteAdapter adapter;
    private Handler handler;
    private AutocompleteSessionToken sessionToken;
    private OnAddressSelectedListener listener;
    
    // Country name mapping
    private static final String[][] COUNTRY_MAPPINGS = {
        {"Canada", "CA"},
        {"United States", "US"},
        {"United Kingdom", "GB"},
        {"Australia", "AU"},
        {"Germany", "DE"},
        {"France", "FR"},
        {"Japan", "JP"},
        {"Brazil", "BR"},
        {"India", "IN"},
        {"Mexico", "MX"}
    };
    
    /**
     * Interface to handle address selection callbacks
     */
    public interface OnAddressSelectedListener {
        void onAddressSelected(String streetAddress, String city, String state, String zipCode, String country);
    }
    
    /**
     * Constructor for AddressAutocompleteHelper
     * @param activity The activity context
     * @param addressEditText The EditText for street address
     */
    public AddressAutocompleteHelper(Activity activity, EditText addressEditText) {
        this.activity = activity;
        this.addressEditText = addressEditText;
        initializePlaces();
    }
    
    /**
     * Constructor with additional address components
     * @param activity The activity context
     * @param addressEditText The EditText for street address
     * @param cityEditText The EditText for city
     * @param stateEditText The EditText for state/province
     * @param zipCodeEditText The EditText for zip/postal code
     */
    public AddressAutocompleteHelper(Activity activity, EditText addressEditText, 
                                     EditText cityEditText, EditText stateEditText, 
                                     EditText zipCodeEditText) {
        this.activity = activity;
        this.addressEditText = addressEditText;
        this.cityEditText = cityEditText;
        this.stateEditText = stateEditText;
        this.zipCodeEditText = zipCodeEditText;
        initializePlaces();
    }
    
    /**
     * Constructor with country spinner
     * @param activity The activity context
     * @param addressEditText The EditText for street address
     * @param cityEditText The EditText for city
     * @param stateEditText The EditText for state/province
     * @param zipCodeEditText The EditText for zip/postal code
     * @param countrySpinner The Spinner for country
     */
    public AddressAutocompleteHelper(Activity activity, EditText addressEditText, 
                                     EditText cityEditText, EditText stateEditText, 
                                     EditText zipCodeEditText, Spinner countrySpinner) {
        this.activity = activity;
        this.addressEditText = addressEditText;
        this.cityEditText = cityEditText;
        this.stateEditText = stateEditText;
        this.zipCodeEditText = zipCodeEditText;
        this.countrySpinner = countrySpinner;
        initializePlaces();
    }
    
    /**
     * Initialize Google Places SDK
     */
    private void initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(activity.getApplicationContext(), getApiKey(activity));
        }
        placesClient = Places.createClient(activity);
        handler = new Handler(Looper.getMainLooper());
        sessionToken = AutocompleteSessionToken.newInstance();
        setupSuggestionPopup();
    }
    
    /**
     * Get API key from manifest
     */
    private String getApiKey(Context context) {
        try {
            android.content.pm.ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 
                            android.content.pm.PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
                if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_GOOGLE_PLACES_API_KEY")) {
                    return apiKey;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "YOUR_GOOGLE_PLACES_API_KEY";
    }
    
    /**
     * Setup the suggestion popup window
     */
    private void setupSuggestionPopup() {
        suggestionListView = new ListView(activity);
        adapter = new AutocompleteAdapter(activity, new ArrayList<>());
        suggestionListView.setAdapter(adapter);
        suggestionListView.setDividerHeight(0);
        
        suggestionPopup = new PopupWindow(suggestionListView, 
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        suggestionPopup.setBackgroundDrawable(activity.getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));
        suggestionPopup.setOutsideTouchable(true);
        suggestionPopup.setFocusable(false); // Prevent popup from intercepting keyboard events
        
        // Disable keyboard navigation in ListView to prevent spacebar from selecting items
        suggestionListView.setItemsCanFocus(false);
        
        suggestionListView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = adapter.getItem(position);
            if (prediction != null) {
                fetchPlaceDetails(prediction.getPlaceId());
                suggestionPopup.dismiss();
            }
        });
    }
    
    /**
     * Attach autocomplete to an EditText
     */
    public void attachToEditText(EditText editText) {
        if (editText == null || addressEditText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous requests
                handler.removeCallbacksAndMessages(null);
                
                String query = s.toString();
                
                // Dismiss popup if spacebar was just pressed (space at the end)
                // This prevents spacebar from selecting the first suggestion
                if (query.endsWith(" ")) {
                    suggestionPopup.dismiss();
                    return;
                }
                
                final String trimmedQuery = query.trim();
                if (trimmedQuery.length() >= 3) {
                    // Delay to avoid too many API calls
                    handler.postDelayed(() -> {
                        String currentText = editText.getText().toString().trim();
                        if (trimmedQuery.equals(currentText)) {
                            getPlacePredictions(trimmedQuery);
                        }
                    }, 300);
                } else {
                    suggestionPopup.dismiss();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Small delay before dismissing to allow item selection
                handler.postDelayed(() -> suggestionPopup.dismiss(), 200);
            }
        });
        
        // Handle key events to prevent spacebar from selecting items
        editText.setOnKeyListener((v, keyCode, event) -> {
            // Dismiss popup when spacebar is pressed
            if (keyCode == android.view.KeyEvent.KEYCODE_SPACE && 
                event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                suggestionPopup.dismiss();
                // Allow the space character to be typed normally
                return false;
            }
            return false;
        });
    }
    
    /**
     * Get place predictions from Google Places API
     */
    private void getPlacePredictions(String query) {
        if (placesClient == null || TextUtils.isEmpty(query)) return;
        
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(sessionToken)
                .build();
        
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    if (predictions != null && !predictions.isEmpty()) {
                        adapter.clear();
                        adapter.addAll(predictions);
                        adapter.notifyDataSetChanged();
                        showSuggestions();
                    } else {
                        suggestionPopup.dismiss();
                    }
                })
                .addOnFailureListener(exception -> {
                    // Silently fail - don't show error to user
                    suggestionPopup.dismiss();
                });
    }
    
    /**
     * Show suggestion dropdown
     */
    private void showSuggestions() {
        if (addressEditText == null || suggestionPopup == null) return;
        
        if (!suggestionPopup.isShowing()) {
            int[] location = new int[2];
            addressEditText.getLocationOnScreen(location);
            
            // Calculate popup position
            int popupWidth = addressEditText.getWidth();
            int popupHeight = Math.min(400, adapter.getCount() * 80); // Limit height
            
            suggestionPopup.setWidth(popupWidth);
            suggestionPopup.showAsDropDown(addressEditText, 0, 0);
        }
    }
    
    /**
     * Fetch full place details when a prediction is selected
     */
    private void fetchPlaceDetails(String placeId) {
        if (placesClient == null || TextUtils.isEmpty(placeId)) return;
        
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS
        );
        
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build();
        
        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    if (place != null) {
                        handlePlaceSelection(place);
                        // Create new session token for next search
                        sessionToken = AutocompleteSessionToken.newInstance();
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(activity, "Failed to fetch address details", Toast.LENGTH_SHORT).show();
                });
    }

    private void handlePlaceSelection(Place place) {
        if (place == null) return;
        
        String streetAddress = "";
        String city = "";
        String state = "";
        String zipCode = "";
        String country = "";
        
        // Parse address components
        AddressComponents components = place.getAddressComponents();
        if (components != null) {
            String streetNumber = "";
            String route = "";
            
            for (com.google.android.libraries.places.api.model.AddressComponent component : components.asList()) {
                List<String> types = component.getTypes();
                
                if (types.contains("street_number")) {
                    streetNumber = component.getName();
                }
                
                if (types.contains("route")) {
                    route = component.getName();
                }
                
                if (types.contains("locality")) {
                    city = component.getName();
                }
                
                if (types.contains("administrative_area_level_1")) {
                    state = component.getShortName();
                }
                
                if (types.contains("postal_code")) {
                    zipCode = component.getName();
                }
                
                if (types.contains("country")) {
                    country = component.getName();
                }
            }
            
            // Build street address
            if (!TextUtils.isEmpty(streetNumber) && !TextUtils.isEmpty(route)) {
                streetAddress = streetNumber + " " + route;
            } else if (!TextUtils.isEmpty(route)) {
                streetAddress = route;
            } else if (!TextUtils.isEmpty(streetNumber)) {
                streetAddress = streetNumber;
            }
        }
        
        // Fallback to full address if street address is empty
        if (TextUtils.isEmpty(streetAddress)) {
            String fullAddress = place.getAddress();
            if (fullAddress != null) {
                streetAddress = fullAddress;
            }
        }
        
        // Populate the EditText fields
        if (addressEditText != null && !TextUtils.isEmpty(streetAddress)) {
            addressEditText.setText(streetAddress);
        }
        
        if (cityEditText != null && !TextUtils.isEmpty(city)) {
            cityEditText.setText(city);
        }
        
        if (stateEditText != null && !TextUtils.isEmpty(state)) {
            stateEditText.setText(state);
        }
        
        if (zipCodeEditText != null && !TextUtils.isEmpty(zipCode)) {
            zipCodeEditText.setText(zipCode);
        }
        
        // Set country spinner if available
        if (countrySpinner != null && !TextUtils.isEmpty(country)) {
            setCountryFromName(country);
        }
        
        // Notify listener if set
        if (listener != null) {
            listener.onAddressSelected(streetAddress, city, state, zipCode, country);
        }
    }
    
    /**
     * Set country spinner based on country name
     */
    private void setCountryFromName(String countryName) {
        if (countrySpinner == null) return;
        
        try {
            android.widget.ArrayAdapter<String> adapter = (android.widget.ArrayAdapter<String>) countrySpinner.getAdapter();
            if (adapter != null) {
                // Try to find exact match first
                for (int i = 0; i < adapter.getCount(); i++) {
                    String item = adapter.getItem(i);
                    if (item != null && item.equalsIgnoreCase(countryName)) {
                        countrySpinner.setSelection(i);
                        return;
                    }
                }
                
                // Try partial match
                for (int i = 0; i < adapter.getCount(); i++) {
                    String item = adapter.getItem(i);
                    if (item != null && countryName.toLowerCase().contains(item.toLowerCase()) ||
                        item != null && item.toLowerCase().contains(countryName.toLowerCase())) {
                        countrySpinner.setSelection(i);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Set listener for address selection callbacks
     */
    public void setOnAddressSelectedListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Custom adapter for autocomplete predictions
     */
    private class AutocompleteAdapter extends ArrayAdapter<AutocompletePrediction> {
        
        public AutocompleteAdapter(Context context, List<AutocompletePrediction> predictions) {
            super(context, android.R.layout.simple_list_item_1, predictions);
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            
            AutocompletePrediction prediction = getItem(position);
            if (prediction != null) {
                TextView textView = (TextView) convertView;
                textView.setText(prediction.getFullText(null));
                textView.setPadding(16, 16, 16, 16);
            }
            
            return convertView;
        }
    }
}

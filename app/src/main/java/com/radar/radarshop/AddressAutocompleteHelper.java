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
    private boolean isAttached = false; // Track if autocomplete is already attached
    
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
        try {
            String apiKey = getApiKey(activity);
            if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GOOGLE_PLACES_API_KEY")) {
                android.util.Log.e("AddressAutocompleteHelper", "Google Places API key not configured");
                return;
            }
            
            if (!Places.isInitialized()) {
                Places.initialize(activity.getApplicationContext(), apiKey);
                android.util.Log.d("AddressAutocompleteHelper", "Google Places SDK initialized");
            }
            placesClient = Places.createClient(activity);
            handler = new Handler(Looper.getMainLooper());
            sessionToken = AutocompleteSessionToken.newInstance();
            setupSuggestionPopup();
        } catch (Exception e) {
            android.util.Log.e("AddressAutocompleteHelper", "Error initializing Places SDK: " + e.getMessage(), e);
        }
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
        suggestionListView.setDividerHeight(1);
        suggestionListView.setDivider(activity.getResources().getDrawable(android.R.drawable.divider_horizontal_dark));
        
        // Create a container with padding for better appearance
        android.widget.LinearLayout container = new android.widget.LinearLayout(activity);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.addView(suggestionListView);
        container.setPadding(0, 0, 0, 0);
        
        suggestionPopup = new PopupWindow(container, 
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        
        // Set background with elevation for visibility
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(android.graphics.Color.WHITE);
        background.setCornerRadius(8f);
        suggestionPopup.setBackgroundDrawable(background);
        
        // Add elevation shadow
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            suggestionPopup.setElevation(8f);
        }
        
        suggestionPopup.setOutsideTouchable(true);
        suggestionPopup.setFocusable(false); // Prevent popup from intercepting keyboard events
        suggestionPopup.setTouchable(true);
        suggestionPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        
        // Disable keyboard navigation in ListView to prevent spacebar from selecting items
        suggestionListView.setItemsCanFocus(false);
        
        suggestionListView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = adapter.getItem(position);
            if (prediction != null) {
                android.util.Log.d("AddressAutocompleteHelper", "Prediction selected: " + prediction.getFullText(null));
                fetchPlaceDetails(prediction.getPlaceId());
                suggestionPopup.dismiss();
            }
        });
    }
    
    /**
     * Attach autocomplete to an EditText
     */
    public void attachToEditText(EditText editText) {
        attachToEditText(editText, false);
    }
    
    /**
     * Attach autocomplete to an EditText
     * @param editText The EditText to attach to
     * @param forceReattach If true, reattach even if already attached
     */
    public void attachToEditText(EditText editText, boolean forceReattach) {
        if (editText == null || addressEditText == null) {
            android.util.Log.w("AddressAutocompleteHelper", "Cannot attach: editText or addressEditText is null");
            return;
        }
        
        if (placesClient == null) {
            android.util.Log.w("AddressAutocompleteHelper", "Cannot attach: Places client not initialized");
            return;
        }
        
        // If already attached to this EditText and not forcing reattach, skip
        if (isAttached && editText == addressEditText && !forceReattach) {
            android.util.Log.d("AddressAutocompleteHelper", "Autocomplete already attached to this EditText");
            return;
        }
        
        // Mark as attached
        isAttached = true;
        
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
                    android.util.Log.d("AddressAutocompleteHelper", "Query length >= 3, will fetch predictions: " + trimmedQuery);
                    // Delay to avoid too many API calls
                    handler.postDelayed(() -> {
                        String currentText = editText.getText().toString().trim();
                        if (trimmedQuery.equals(currentText)) {
                            android.util.Log.d("AddressAutocompleteHelper", "Fetching predictions for: " + currentText);
                            getPlacePredictions(trimmedQuery);
                        } else {
                            android.util.Log.d("AddressAutocompleteHelper", "Text changed, skipping prediction: " + currentText + " vs " + trimmedQuery);
                        }
                    }, 300);
                } else {
                    android.util.Log.d("AddressAutocompleteHelper", "Query too short, dismissing popup");
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
        
        android.util.Log.d("AddressAutocompleteHelper", "Autocomplete attached to EditText successfully");
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
                    android.util.Log.d("AddressAutocompleteHelper", 
                        "Received " + (predictions != null ? predictions.size() : 0) + " predictions for query: " + query);
                    
                    if (predictions != null && !predictions.isEmpty()) {
                        // Run on UI thread to update adapter
                        activity.runOnUiThread(() -> {
                            adapter.clear();
                            adapter.addAll(predictions);
                            adapter.notifyDataSetChanged();
                            android.util.Log.d("AddressAutocompleteHelper", 
                                "Adapter updated with " + predictions.size() + " items");
                            showSuggestions();
                        });
                    } else {
                        android.util.Log.d("AddressAutocompleteHelper", "No predictions found");
                        activity.runOnUiThread(() -> suggestionPopup.dismiss());
                    }
                })
                .addOnFailureListener(exception -> {
                    // Log error for debugging
                    android.util.Log.e("AddressAutocompleteHelper", "Error fetching predictions: " + exception.getMessage(), exception);
                    exception.printStackTrace();
                    
                    activity.runOnUiThread(() -> {
                        suggestionPopup.dismiss();
                        
                        // Check if it's a network connectivity issue
                        String errorMessage = exception.getMessage();
                        if (errorMessage != null && (errorMessage.contains("UnknownHostException") || 
                            errorMessage.contains("No address associated with hostname") ||
                            errorMessage.contains("NoConnectionError"))) {
                            // Show user-friendly message about network connectivity
                            android.util.Log.w("AddressAutocompleteHelper", 
                                "Network connectivity issue - please check internet connection");
                            // Don't show toast to avoid annoying user, just log
                        } else {
                            // Other API errors - could be API key, quota, etc.
                            android.util.Log.w("AddressAutocompleteHelper", 
                                "API error: " + errorMessage);
                        }
                    });
                });
    }
    
    /**
     * Show suggestion dropdown
     */
    private void showSuggestions() {
        if (addressEditText == null || suggestionPopup == null || adapter == null) {
            android.util.Log.w("AddressAutocompleteHelper", "Cannot show suggestions: null check failed");
            return;
        }
        
        if (adapter.getCount() == 0) {
            android.util.Log.d("AddressAutocompleteHelper", "No suggestions to show");
            suggestionPopup.dismiss();
            return;
        }
        
        // Run on UI thread to ensure view is laid out
        addressEditText.post(() -> {
            try {
                // Dismiss any existing popup first
                if (suggestionPopup.isShowing()) {
                    suggestionPopup.dismiss();
                }
                
                // Wait a bit for any dismissal to complete
                addressEditText.postDelayed(() -> {
                    try {
                        // Find the anchor view - could be EditText or its TextInputLayout parent
                        View anchorView = addressEditText;
                        
                        // Try to find TextInputLayout parent for better positioning
                        View parent = (View) addressEditText.getParent();
                        while (parent != null) {
                            if (parent.getClass().getName().contains("TextInputLayout")) {
                                anchorView = parent;
                                android.util.Log.d("AddressAutocompleteHelper", "Using TextInputLayout as anchor");
                                break;
                            }
                            if (parent.getParent() instanceof View) {
                                parent = (View) parent.getParent();
                            } else {
                                break;
                            }
                        }
                        
                        // Get the width of the anchor view
                        int popupWidth = anchorView.getWidth();
                        if (popupWidth <= 0) {
                            // Measure the view if width is 0
                            anchorView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                            popupWidth = anchorView.getMeasuredWidth();
                        }
                        
                        if (popupWidth <= 0) {
                            // Fallback to screen width minus padding
                            android.util.DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                            popupWidth = metrics.widthPixels - (int)(40 * metrics.density); // 20dp padding on each side
                            android.util.Log.d("AddressAutocompleteHelper", "Using screen width fallback: " + popupWidth);
                        }
                        
                        // Calculate height based on number of items, with max limit
                        int itemHeight = (int)(60 * activity.getResources().getDisplayMetrics().density); // 60dp per item
                        int calculatedHeight = adapter.getCount() * itemHeight;
                        int maxHeight = (int)(400 * activity.getResources().getDisplayMetrics().density); // 400dp max
                        int popupHeight = Math.min(calculatedHeight, maxHeight);
                        
                        // Ensure minimum height
                        int minHeight = (int)(100 * activity.getResources().getDisplayMetrics().density);
                        if (popupHeight < minHeight) {
                            popupHeight = minHeight;
                        }
                        
                        android.util.Log.d("AddressAutocompleteHelper", 
                            String.format("Showing popup: width=%d, height=%d, items=%d, anchor=%s", 
                                popupWidth, popupHeight, adapter.getCount(), anchorView.getClass().getSimpleName()));
                        
                        suggestionPopup.setWidth(popupWidth);
                        suggestionPopup.setHeight(popupHeight);
                        
                        // Show popup below the anchor view
                        suggestionPopup.showAsDropDown(anchorView, 0, 0);
                        
                        // Verify popup is showing
                        if (suggestionPopup.isShowing()) {
                            android.util.Log.d("AddressAutocompleteHelper", "Popup is now showing");
                        } else {
                            android.util.Log.w("AddressAutocompleteHelper", "Popup failed to show");
                        }
                        
                    } catch (Exception e) {
                        android.util.Log.e("AddressAutocompleteHelper", "Error showing suggestions: " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }, 50); // Small delay to ensure dismissal is complete
                
            } catch (Exception e) {
                android.util.Log.e("AddressAutocompleteHelper", "Error in post: " + e.getMessage(), e);
                e.printStackTrace();
            }
        });
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
        if (countrySpinner == null || TextUtils.isEmpty(countryName)) return;
        
        try {
            android.widget.ArrayAdapter<String> adapter = (android.widget.ArrayAdapter<String>) countrySpinner.getAdapter();
            if (adapter != null) {
                String normalizedCountryName = countryName.trim();
                
                // Try to find exact match first
                for (int i = 0; i < adapter.getCount(); i++) {
                    String item = adapter.getItem(i);
                    if (item != null && item.equalsIgnoreCase(normalizedCountryName)) {
                        countrySpinner.setSelection(i);
                        android.util.Log.d("AddressAutocompleteHelper", "Country matched exactly: " + item);
                        return;
                    }
                }
                
                // Try partial match (country name contains spinner item or vice versa)
                for (int i = 0; i < adapter.getCount(); i++) {
                    String item = adapter.getItem(i);
                    if (item != null) {
                        String lowerItem = item.toLowerCase();
                        String lowerCountry = normalizedCountryName.toLowerCase();
                        if (lowerCountry.contains(lowerItem) || lowerItem.contains(lowerCountry)) {
                            countrySpinner.setSelection(i);
                            android.util.Log.d("AddressAutocompleteHelper", "Country matched partially: " + item + " from " + normalizedCountryName);
                            return;
                        }
                    }
                }
                
                // Try mapping common country name variations
                String mappedCountry = mapCountryName(normalizedCountryName);
                if (!TextUtils.isEmpty(mappedCountry)) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        String item = adapter.getItem(i);
                        if (item != null && item.equalsIgnoreCase(mappedCountry)) {
                            countrySpinner.setSelection(i);
                            android.util.Log.d("AddressAutocompleteHelper", "Country mapped: " + item + " from " + normalizedCountryName);
                            return;
                        }
                    }
                }
                
                android.util.Log.w("AddressAutocompleteHelper", "Could not match country: " + normalizedCountryName);
            }
        } catch (Exception e) {
            android.util.Log.e("AddressAutocompleteHelper", "Error setting country: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    /**
     * Map common country name variations to standard names
     */
    private String mapCountryName(String countryName) {
        if (TextUtils.isEmpty(countryName)) return null;
        
        String lower = countryName.toLowerCase();
        
        // Map common variations
        if (lower.contains("united states") || lower.contains("usa") || lower.contains("u.s.") || lower.contains("us")) {
            return "United States";
        } else if (lower.contains("united kingdom") || lower.contains("uk") || lower.contains("britain") || lower.contains("england")) {
            return "United Kingdom";
        } else if (lower.equals("canada") || lower.equals("ca")) {
            return "Canada";
        } else if (lower.contains("australia") || lower.equals("au")) {
            return "Australia";
        } else if (lower.contains("germany") || lower.equals("de") || lower.contains("deutschland")) {
            return "Germany";
        } else if (lower.contains("france") || lower.equals("fr")) {
            return "France";
        } else if (lower.contains("japan") || lower.equals("jp")) {
            return "Japan";
        } else if (lower.contains("brazil") || lower.equals("br")) {
            return "Brazil";
        } else if (lower.contains("india") || lower.equals("in")) {
            return "India";
        } else if (lower.contains("mexico") || lower.equals("mx")) {
            return "Mexico";
        }
        
        return null;
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


/**
 * Created by YuujiSakakibara on 2015/08/23.
 */
public class FragmentTestFit extends Fragment {

    private static final int REQUEST_OAUTH = 1;

    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";

    private static final String TAG = "G_FITNESS";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "Connecting...");
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            Log.i(TAG, "Disconnected");
            mClient.disconnect();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildFitnessClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_fit, container, false);
        view.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeRecordingAPI();
            }
        });
        view.findViewById(R.id.test2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });
        return view;
    }

    public boolean onNavigationItemSelected(MenuItem menu) {
        return false;
    }

    public int getNavigationMenuId() {
        return 0;
    }


    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SESSIONS_API)
                .addApi(LocationServices.API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_NUTRITION_READ_WRITE))
                .addConnectionCallbacks(fitnessConCallbacks())
                .addOnConnectionFailedListener(fitnessConFailed()).build();
    }

    private GoogleApiClient.ConnectionCallbacks fitnessConCallbacks() {
        return new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.i(TAG, "Connected");
            }

            @Override
            public void onConnectionSuspended(int i) {
                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                }

            }
        };
    }

    private GoogleApiClient.OnConnectionFailedListener fitnessConFailed() {
        return new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                if (!result.hasResolution()) {
                    // Show the localized error dialog
                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                            getActivity(), 0).show();
                    return;
                }
                // The failure has a resolution. Resolve it.
                // Called typically when the app is not yet authorized, and an
                // authorization dialog is displayed to the user.
                if (!authInProgress) {
                    try {
                        Log.i(TAG, "Attempting to resolve failed connection");
                        authInProgress = true;
                        result.startResolutionForResult(getActivity(), REQUEST_OAUTH);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG,
                                "Exception while starting resolution activity", e);
                    }
                }

            }
        };
    }

    private void readData(){
        ReadData read = new ReadData();
        read.execute();
    }

    private class ReadData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            readDataRequest();
            return null;
        }
    }

    private void readDataRequest(){
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder().aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        DataReadResult dataReadResult =
                Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);

    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private void subscribeRecordingAPI() {
        Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    if (status.getStatusCode()
                            == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                        Log.i(TAG, "Existing subscription for activity detected.");
                        unsubscribeRecordingAPI();
                    } else {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                } else {
                    Log.i(TAG, "There was a problem subscribing. " + status.getStatusMessage());
                }

            }
        });
    }

    private void unsubscribeRecordingAPI() {
        Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Successfully unsubscribed for data type: SAMPLE");
                        } else {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe for data type: SAMPLE");
                        }
                    }
                });
    }

    private void getActiveSubscriptionList(){
        Fitness.RecordingApi.listSubscriptions(mClient, DataType.TYPE_ACTIVITY_SAMPLE)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
    }
}

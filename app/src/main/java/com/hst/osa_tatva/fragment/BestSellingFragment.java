package com.hst.osa_tatva.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hst.osa_tatva.R;
import com.hst.osa_tatva.activity.ProductDetailActivity;
import com.hst.osa_tatva.adapter.BestSellingListAdapter;
import com.hst.osa_tatva.bean.support.Product;
import com.hst.osa_tatva.bean.support.ProductList;
import com.hst.osa_tatva.helpers.AlertDialogHelper;
import com.hst.osa_tatva.helpers.ProgressDialogHelper;
import com.hst.osa_tatva.interfaces.DialogClickListener;
import com.hst.osa_tatva.servicehelpers.ServiceHelper;
import com.hst.osa_tatva.serviceinterfaces.IServiceListener;
import com.hst.osa_tatva.utils.OSAConstants;
import com.hst.osa_tatva.utils.PreferenceStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.util.Log.d;

public class BestSellingFragment extends Fragment implements IServiceListener, DialogClickListener, BestSellingListAdapter.OnItemClickListener{

    private static final String TAG = BestSellingFragment.class.getName();
    Context context;
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;
    private Handler mHandler = new Handler();
    int totalCount = 0, checkrun = 0;
    protected boolean isLoadingForFirstTime = true;

    private ArrayList<Product> productArrayList = new ArrayList<>();
    Product product;
    ProductList productList;
    private RecyclerView recyclerViewPopularProduct;
    private View rootView;
    private TextView itemCount;

    public static BestSellingFragment newInstance(int position) {
        BestSellingFragment frag = new BestSellingFragment();
        Bundle b = new Bundle();
        b.putInt("position", position);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_best_selling, container, false);
        initiateServices();

//        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.activity_toolbar);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_left_arrow));
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Fragment newFragment = null;
//                newFragment = new DashboardFragment();
//                replaceFragment(newFragment);
//            }
//        });

        recyclerViewPopularProduct = (RecyclerView) rootView.findViewById(R.id.listView_best_selling);
        itemCount = (TextView) rootView.findViewById(R.id.item_count);
        getDashboardServices();
//        loadMob();
        return rootView;
    }

    public void initiateServices() {

        serviceHelper = new ServiceHelper(getActivity());
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(getActivity());

    }

    private boolean validateSignInResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(OSAConstants.PARAM_MESSAGE);
                d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(rootView.getContext(), msg);

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(final JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateSignInResponse(response)) {
            try {
                Gson gson = new Gson();

                JSONObject popularObjData = response.getJSONObject("popular_product_list");
                productList = gson.fromJson(popularObjData.toString(), ProductList.class);
                productArrayList.addAll(productList.getProductArrayList());
                BestSellingListAdapter adasd = new BestSellingListAdapter(getActivity(), productArrayList, this);
                GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 4);
                mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (adasd.getItemViewType(position) > 0) {
                            return adasd.getItemViewType(position);
                        } else {
                            return 1;
                        }
                        //return 2;
                    }
                });
                recyclerViewPopularProduct.setLayoutManager(mLayoutManager);
                recyclerViewPopularProduct.setAdapter(adasd);
                itemCount.setText(productArrayList.size() + " Items");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "Error while sign In");
        }
    }

    @Override
    public void onError(String error) {

    }

//    public void reLoadPage() {
//        startActivity();
//    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    @Override
    public void onItemClickBestSelling(View view, int position) {
        Product product = null;
        product = productArrayList.get(position);
        Intent intent;
        intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("page", "best");
        intent.putExtra("productObj", product.getid());
        startActivity(intent);
    }

    private void getDashboardServices() {
        JSONObject jsonObject = new JSONObject();
        String id = PreferenceStorage.getUserId(getActivity());
        try {
//            jsonObject.put(SkilExConstants.USER_MASTER_ID, PreferenceStorage.getUserId(getActivity()));
            jsonObject.put(OSAConstants.KEY_USER_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = OSAConstants.BUILD_URL + OSAConstants.DASHBOARD;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, someFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}

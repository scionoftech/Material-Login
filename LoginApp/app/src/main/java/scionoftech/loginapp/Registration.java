package scionoftech.loginapp;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class Registration extends Fragment {


    public Registration() {
        // Required empty public constructor
    }

    String transitionName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);


        final Bundle bundle = getArguments();
        if (bundle != null) {
            transitionName = bundle.getString("TRANS_NAME");
        }


        final FrameLayout clear = (FrameLayout) view.findViewById(R.id.clear);

        //set transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clear.setTransitionName(transitionName);
        }

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //fab.setTransitionName("trans_clear");
                    Login endFragment = new Login();
                    setSharedElementReturnTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(R.transition.change_image_trans));
                    setExitTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(android.R.transition.fade));

                    endFragment.setSharedElementEnterTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(R.transition.change_image_trans));
                    endFragment.setEnterTransition(TransitionInflater.from(
                            getActivity()).inflateTransition(android.R.transition.fade));

                   // TransitionName = fab.getTransitionName();


                    Bundle bundle = new Bundle();
                    bundle.putString("TRANS_NAME", transitionName);
                    endFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, endFragment)
                            .addSharedElement(clear, transitionName)
                            .commit();
                }
            }
        });


        return view;
    }

}

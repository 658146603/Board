package xyz.qscftyjm.board


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 */
class MainChatFragment : Fragment() {
    private var view: View? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main_chat, container, false)


        return view
    }

    companion object {

        private val TAG = "Board"
    }

}// Required empty public constructor

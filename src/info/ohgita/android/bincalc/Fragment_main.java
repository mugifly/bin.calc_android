package info.ohgita.android.bincalc;

import java.util.LinkedList;

import info.ohgita.android.bincalc.calculator.BaseConvResult;
import info.ohgita.android.bincalc.calculator.BaseConverter;
import info.ohgita.android.bincalc.calculator.HistoryItem;
import info.ohgita.bincalc_android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

final public class Fragment_main extends SherlockFragment implements
		OnClickListener, OnLongClickListener {
	final static int ID_BASETYPE_BIN = 2;
	final static int ID_BASETYPE_DEC = 10;
	final static int ID_BASETYPE_HEX = 16;

	private static final String STATE_KEY_BASETYPE = "BASETYPE";
	private static final String STATE_KEY_BASEINPUT_VALUE = "BASEINP_VAL";
	private static final String STATE_KEY_MEMORY_BASETYPE = "MEMORY_BASETYPE";
	private static final String STATE_KEY_MEMORY_VALUE = "MEMORY_VAL";

	boolean is_init = false;

	String defaultValue;
	int selectedBasetypeId = ID_BASETYPE_DEC;

	int currentOperationModeId = -1;
	static int ID_OPRMODE_PLUS = 1;
	static int ID_OPRMODE_MINUS = 2;
	static int ID_OPRMODE_MULTI = 3;
	static int ID_OPRMODE_DIVIS = 4;
	static int ID_OPRMODE_MEMORY_IN = 5;
	static int ID_OPRMODE_MEMORY_CLEAR = 6;
	static int ID_OPRMODE_MEMORY_READ = 7;
	
	int DEFAULT_VIBRATION_MSEC = 20;

	boolean prefKeyVibration = false;
	boolean prefSaveState = false;

	Calculator calc;
	BaseConverter baseconv;

	View v = null;

	Vibrator vib;

	ViewPager baseinputsViewPager;
	public LinearLayout baseinputsViewPager_LinearLayout;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("binCalc", "Fragment_main - onCreateView()");

		is_init = false;

		/* Load preferences */
		loadPreferences();

		/* initialize calculator class */
		calc = new Calculator();
		
		/* Load default value */
		if (savedInstanceState != null) {
			/* Load a state from the savedInstanceState */
			// Base-type ID
			selectedBasetypeId = savedInstanceState.getInt(STATE_KEY_BASETYPE);
			Log.d("binCalc",
					"Fragment_main - onCreateView() - Default BasetypeId: "
							+ selectedBasetypeId);
			// Value
			defaultValue = savedInstanceState
					.getString(STATE_KEY_BASEINPUT_VALUE);
			Log.d("binCalc", "Fragment_main - onCreateView() - Default value: "
					+ defaultValue);
			
			/* Memory data */
			int memory_base_type = savedInstanceState.getInt(STATE_KEY_MEMORY_BASETYPE);
			String memory_value = savedInstanceState.getString(STATE_KEY_MEMORY_VALUE);
			if (memory_base_type != -1 && memory_value != null) {
				calc.InMemory(memory_base_type, memory_value);
			}
		} else {
			/* Load a state from kept state */
			if (prefSaveState) {
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(getActivity()
								.getApplicationContext());
				// Base-type ID
				selectedBasetypeId = pref.getInt(STATE_KEY_BASETYPE,
						selectedBasetypeId);
				// Value
				defaultValue = pref
						.getString(STATE_KEY_BASEINPUT_VALUE, null);
				Log.d("binCalc",
						"Fragment_main - onCreateView() - Default value: "
								+ defaultValue);
				
				/* Memory data */
				int memory_base_type = pref.getInt(STATE_KEY_MEMORY_BASETYPE, -1);
				String memory_value = pref.getString(STATE_KEY_MEMORY_VALUE, null);
				if (memory_base_type != -1 && memory_value != null) {
					calc.InMemory(memory_base_type, memory_value);
				}
			}
		}
		
		/* Inflate a Fragment */
		v = inflater.inflate(R.layout.fragment_main_portrait, container);

		/* Initialize the baseinputsViewPager */
		baseinputsViewPager = (ViewPager) v
				.findViewById(R.id.baseinputsViewPager);
		PagerAdapter mPagerAdapter = new Adapter_BaseinputsViewPager(
				v.getContext(), this);
		baseinputsViewPager.setAdapter(mPagerAdapter);

		/* Set a listener for the baseinputsViewPager */
		PageListener pageListener = new PageListener();
		baseinputsViewPager.setOnPageChangeListener(pageListener);

		/* Event handler for Base-type ToggleButtons */
		final ToggleButton tb_bin = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_bin);
		final ToggleButton tb_dec = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_dec);
		final ToggleButton tb_hex = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_hex);
		tb_bin.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == true) {
					Log.d("binCalc",
							"Fragment_main - ToggleButton onCheckedChanged = true ... BIN");
					switchBasetype(ID_BASETYPE_BIN);
				} else if (selectedBasetypeId == ID_BASETYPE_BIN) {
					buttonView.setChecked(true);
				}
			}
		});
		tb_dec.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == true) {
					Log.d("binCalc",
							"Fragment_main - ToggleButton onCheckedChanged = true ... DEC");
					switchBasetype(ID_BASETYPE_DEC);
				} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
					buttonView.setChecked(true);
				}
			}
		});
		tb_hex.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked == true) {
					Log.d("binCalc",
							"Fragment_main - ToggleButton onCheckedChanged = true ... HEX");
					switchBasetype(ID_BASETYPE_HEX);
				} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
					buttonView.setChecked(true);
				}
			}
		});

		/* set Event-handler for key-buttons */
		v.findViewById(R.id.keyButton0).setOnClickListener(this);
		v.findViewById(R.id.keyButton1).setOnClickListener(this);
		v.findViewById(R.id.keyButton2).setOnClickListener(this);
		v.findViewById(R.id.keyButton3).setOnClickListener(this);
		v.findViewById(R.id.keyButton4).setOnClickListener(this);
		v.findViewById(R.id.keyButton5).setOnClickListener(this);
		v.findViewById(R.id.keyButton6).setOnClickListener(this);
		v.findViewById(R.id.keyButton7).setOnClickListener(this);
		v.findViewById(R.id.keyButton8).setOnClickListener(this);
		v.findViewById(R.id.keyButton9).setOnClickListener(this);
		v.findViewById(R.id.keyButtonA).setOnClickListener(this);
		v.findViewById(R.id.keyButtonB).setOnClickListener(this);
		v.findViewById(R.id.keyButtonC).setOnClickListener(this);
		v.findViewById(R.id.keyButtonD).setOnClickListener(this);
		v.findViewById(R.id.keyButtonE).setOnClickListener(this);
		v.findViewById(R.id.keyButtonF).setOnClickListener(this);
		v.findViewById(R.id.keyButtonOpPl).setOnClickListener(this);
		v.findViewById(R.id.keyButtonOpMi).setOnClickListener(this);
		v.findViewById(R.id.keyButtonOpMp).setOnClickListener(this);
		v.findViewById(R.id.keyButtonOpDi).setOnClickListener(this);
		v.findViewById(R.id.keyButtonEq).setOnClickListener(this);
		v.findViewById(R.id.keyButtonPo).setOnClickListener(this);
		v.findViewById(R.id.keyButtonMC).setOnClickListener(this);
		v.findViewById(R.id.keyButtonMin).setOnClickListener(this);
		v.findViewById(R.id.keyButtonMR).setOnClickListener(this);

		/* Set event-handler to backspace button (ImageButton) */
		ImageView bs = (ImageView) v
				.findViewById(R.id.imageButton_baseinput_backspace);
		bs.setOnClickListener(this);
		bs.setOnLongClickListener(this);

		/* initialize base-converter class */
		baseconv = new BaseConverter();

		/* initialize vibration */
		vib = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);

		/* return inflated view */
		return v;
	}

	/**
	 * OnSave instance state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("binCalc", "Fragment_main - onSaveInstanceState()");
		// Base-type ID
		outState.putInt(STATE_KEY_BASETYPE, selectedBasetypeId);
		// Value
		outState.putString(STATE_KEY_BASEINPUT_VALUE,
				getCurrent_Baseinput_EditText().getEditableText().toString());
		
		/* Save a memory */
		CalculatorMemoryData memory = calc.readMemory();
		if (memory == null) {
			// Memory base-type ID
			outState.putInt(STATE_KEY_MEMORY_BASETYPE, -1);
			// Memory value
			outState.putString(STATE_KEY_MEMORY_VALUE, "");
		} else {
			// Memory base-type ID
			outState.putInt(STATE_KEY_MEMORY_BASETYPE, memory.base_type);
			// Memory value
			outState.putString(STATE_KEY_MEMORY_VALUE, memory.value);
		}
	}

	/**
	 * OnPause (Fragment has paused. It maybe fragment has pop out from a
	 * stack.)
	 */
	@Override
	public void onPause() {
		if (prefSaveState) {
			Log.d("binCalc", "Fragment_main - onPause - Keep state");
			// Keep state
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(getActivity()
							.getApplicationContext());
			Editor e = pref.edit();
			
			// Base-type ID
			e.putInt(STATE_KEY_BASETYPE, selectedBasetypeId);
			// Value
			e.putString(STATE_KEY_BASEINPUT_VALUE,
					getCurrent_Baseinput_EditText().getEditableText()
							.toString());
			
			/* Save a memory */
			CalculatorMemoryData memory = calc.readMemory();
			if (memory == null) {
				// Memory base-type ID
				e.putInt(STATE_KEY_MEMORY_BASETYPE, -1);
				// Memory value
				e.putString(STATE_KEY_MEMORY_VALUE, "");
			} else {
				// Memory base-type ID
				e.putInt(STATE_KEY_MEMORY_BASETYPE, memory.base_type);
				// Memory value
				e.putString(STATE_KEY_MEMORY_VALUE, memory.value);
			}
			
			e.commit();
		}
		super.onPause();
	}

	/**
	 * Load preferences
	 */
	public void loadPreferences() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getActivity()
						.getApplicationContext());
		prefKeyVibration = pref.getBoolean(
				getResources().getString(R.string.pref_item_keyVibration_key),
				false);
		prefSaveState = pref.getBoolean(
				getResources().getString(R.string.pref_item_stateSave_key),
				true);
	}

	/**
	 * Calculate & Convert input base-number
	 */
	public void calculate() {
		Log.d("binCalc", "calculate()");
		
		int current_base_type = selectedBasetypeId;

		/* Backup a current value of the before before-inputs */
		int before_page = baseinputsViewPager.getCurrentItem();
		String before_value = getCurrent_Baseinput_EditText().getText()
				.toString();

		/* Pre-Restore a before value to the before Base-inputs */
		restoreBaseInputsFromHistory(before_page);

		/*
		 * Pre-Save new history (It is provisional history for scroll to next
		 * page.
		 */
		int new_page = before_page + 1;
		HistoryItem history = new HistoryItem();
		history.setBaseType(current_base_type);
		history.setNumberString(""); // Provisional value
		calc.putHistory(new_page, history);
		
		/* Scroll the ViewPager of Base-inputs */
		Log.d("binCalc", "calculate() - Scrolling");
		baseinputsViewPager.arrowScroll(View.FOCUS_RIGHT);
		Log.d("binCalc", "calculate() - Scrolled page to: " + new_page);

		/* Copy a before value to the new base-inputs */
		getCurrent_Baseinput_EditText().setText(before_value);

		/* Before Base convert */
		baseConvert();

		/* Calculate (using Decimal) */
		String dec_value = ((EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_dec)).getText()
				.toString();
		try {
			dec_value = calc.calc(dec_value);
		} catch (NullPointerException e) {
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(
							R.color.main_editText_baseinput_TextColor_error));
		} catch (NumberFormatException e) {
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(
							R.color.main_editText_baseinput_TextColor_error));
		}
		;

		/* Set caluculate result to Decimal EditText */
		((EditText) getCurrent_Baseinputs_ViewPager().findViewById(
				R.id.editText_baseinput_dec)).setText(dec_value);

		/* After Base convert */
		history = baseConvert(ID_BASETYPE_DEC);

		/* Re-Save new history */
		history.setBaseType(current_base_type);
		int history_id = calc.putHistory(new_page, history);
		Log.d("binCalc", "calculate() - Save a history(" + history_id + ") = "
				+ history.getNumberString());
	}

	/**
	 * Convert input base-number
	 */
	public HistoryItem baseConvert() {
		return baseConvert(selectedBasetypeId);
	}

	public HistoryItem baseConvert(int sourceBasetype) {
		Log.d("binCalc", "baseConvert()");
		
		/* Fetch a string of a source number */		
		String value = null;
		if (sourceBasetype == ID_BASETYPE_BIN) {
			value = ((EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_bin)).getText().toString();
		} else if (sourceBasetype == ID_BASETYPE_DEC) {
			value = ((EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_dec)).getText().toString();
		} else if (sourceBasetype == ID_BASETYPE_HEX) {
			value = ((EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_hex)).getText().toString();
		}
		
		EditText et_bin = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_bin);
		EditText et_dec = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_dec);
		EditText et_hex = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_hex);

		et_bin.setTextColor(getResources().getColor(
				R.color.main_editText_baseinput_TextColor_default));
		et_dec.setTextColor(getResources().getColor(
				R.color.main_editText_baseinput_TextColor_default));
		et_hex.setTextColor(getResources().getColor(
				R.color.main_editText_baseinput_TextColor_default));
		
		/* Make an item of the history */
		HistoryItem history = new HistoryItem();
		history.setBaseType(sourceBasetype);
		history.setNumberString(value);
		
		/* Parse formula */
		LinkedList<String> parsedList = null;
		try {
			parsedList = calc.parseToList(value);
		} catch (NullPointerException e) {
			Log.e("binCalc", e.toString(), e);
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(
							R.color.main_editText_baseinput_TextColor_error));
		} catch (NumberFormatException e) {
			Log.e("binCalc", e.toString(), e);
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(
							R.color.main_editText_baseinput_TextColor_error));
		}
		;

		/* Convert */
		try {
			if (sourceBasetype == ID_BASETYPE_BIN) {
				et_bin.setText(calc.listToString(calc.listZeropadding(parsedList, 2), 2)); // for zero-padding && separate
				BaseConvResult dec = calc.listBaseConv(parsedList, 2, 10);
				et_dec.setText(dec.value);
				BaseConvResult hex = calc.listBaseConv(parsedList, 2, 16);
				et_hex.setText(hex.value);
				
				/* Set a value into the history */
				history.setNumberString(ID_BASETYPE_DEC, dec.value);
				history.setNumberString(ID_BASETYPE_HEX, hex.value);
				
			} else if (sourceBasetype == ID_BASETYPE_DEC) {
				BaseConvResult bin = calc.listBaseConv(parsedList, 10, 2);
				et_bin.setText(bin.value);
				et_dec.setText(calc.listToString(parsedList, 10));// for Remove a decimal point
				BaseConvResult hex = calc.listBaseConv(parsedList, 10, 16);
				et_hex.setText(hex.value);
				
				/* Set a value into the history */
				history.setNumberString(ID_BASETYPE_BIN, bin.value);
				history.setNumberString(ID_BASETYPE_HEX, hex.value);
				
			} else if (sourceBasetype == ID_BASETYPE_HEX) {
				BaseConvResult bin = calc.listBaseConv(parsedList, 16, 2);
				et_bin.setText(bin.value);
				BaseConvResult dec = calc.listBaseConv(parsedList, 16, 10);
				et_dec.setText(dec.value);
				
				/* Set a value into the history */
				history.setNumberString(ID_BASETYPE_BIN, bin.value);
				history.setNumberString(ID_BASETYPE_DEC, dec.value);
				
			}
		} catch (Exception e) {
			Log.e("binCalc", e.toString(), e);
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(
							R.color.main_editText_baseinput_TextColor_error));
		}
		
		return history;
	}

	/**
	 * Set value to current baseinput Edittext
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		EditText et = getCurrent_Baseinput_EditText();
		et.setText(value);
		baseConvert();
	}

	/**
	 * All-Clear calculator ( with Inputs, Memories, others... )
	 */
	public void inputAllClear() {
		Log.d("binCalc", "Fragment_main - inputAllClear()");
		calc.clearMemory();
		inputClear();
	}

	/**
	 * Input-Clear calculator
	 */
	public void inputClear() {
		EditText et = getCurrent_Baseinput_EditText();
		if (et == null)
			return;
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			et.setText("0000");
		} else {
			et.setText("0");
		}
		baseConvert();
		updateButtonsState();
	}

	/**
	 * input base-number key
	 * 
	 * @param str
	 *            input-Key
	 */
	public void inputBasenumber(String str) {
		EditText et = getCurrent_Baseinput_EditText();

		/* HEX alphabet */
		if (str.charAt(0) >= 'A' && selectedBasetypeId != ID_BASETYPE_HEX) {
			// if alphabet && BASETYPE is not HEX... cancel
			return;
		}

		/* point */
		if (str.contentEquals(".")) {
			if (et.getText().toString().indexOf('.') == -1) {
				et.setText(et.getText().toString() + ".");
			}
			return;
		}

		/* general number */
		if (et.getText().toString().contentEquals("0")) {
			et.setText(str);
		} else if (et.getText().toString().contentEquals("0000")) {
			et.setText(str);
		} else {
			String res;
			try {
				res = calc.listToString(calc.removeParentheses(calc.parseToList(et.getText().toString())), selectedBasetypeId);
				et.setText(res + str);
			} catch (Exception e) {
				getCurrent_Baseinput_EditText().setTextColor(
						getResources().getColor(	R.color.main_editText_baseinput_TextColor_error));
				return;
			}
		}
		baseConvert();
		updateButtonsState();
	}

	/**
	 * input Backspace key
	 */
	public void inputBackspace() {
		Log.i("binCalc", "Fragment_main - inputBackspace()...");
		EditText et = getCurrent_Baseinput_EditText();
		
		String value = "";
		try {
			value = calc.listToString(calc.removeParentheses(calc.parseToList(et.getText().toString())), selectedBasetypeId);
		} catch (Exception e) {
			getCurrent_Baseinput_EditText().setTextColor(
					getResources().getColor(	R.color.main_editText_baseinput_TextColor_error));
			value = et.getText().toString();
		}

		if (selectedBasetypeId == ID_BASETYPE_BIN) {

			if (value.contentEquals("0000") || value.contentEquals("0001")) {
				// Zero reset
				inputClear();
			} else {
				// Delete a last character
				if (value.substring(value.length() - 4).contentEquals("0000")) {
					et.setText(value.substring(0, value.length() - 4));
				} else {
					et.setText(value.substring(0, value.length() - 1));
				}
				baseConvert();
			}

		} else {

			if (value.length() <= 1) {
				// Zero reset
				inputClear();
			} else {
				// Delete a last character
				et.setText(value.substring(0, value.length() - 1));
				baseConvert();
			}

		}
		
		updateButtonsState();
	}

	/**
	 * input Operation key
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public void inputOpr(int oprmodeId) {
		EditText et = getCurrent_Baseinput_EditText();

		String str = "";
		if (oprmodeId == ID_OPRMODE_PLUS) {
			str = "+";
		} else if (oprmodeId == ID_OPRMODE_MINUS) {
			str = "-";
		} else if (oprmodeId == ID_OPRMODE_MULTI) {
			str = "*";
		} else if (oprmodeId == ID_OPRMODE_DIVIS) {
			str = "/";
		} else if (oprmodeId == ID_OPRMODE_MEMORY_IN) {
			/* Calculate */
			EditText et_dec = (EditText)getCurrent_Baseinputs_ViewPager().findViewById(R.id.editText_baseinput_dec);
			String value = calc.calc(et_dec.getEditableText().toString());
			try {
				value = baseconv.decToN(Double.parseDouble(value), selectedBasetypeId);
				/* Memory in */
				calc.InMemory(selectedBasetypeId, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
			updateButtonsState();
			return;
		} else if (oprmodeId == ID_OPRMODE_MEMORY_CLEAR) {
			calc.clearMemory();
			updateButtonsState();
			return;
		} else if (oprmodeId == ID_OPRMODE_MEMORY_READ) {
			CalculatorMemoryData memory = calc.readMemory();
			if (memory == null) {
				updateButtonsState();
				return;
			}
			String value = memory.value;
			EditText et_ = null;
			switch (memory.base_type) {
				case ID_BASETYPE_BIN:
					et_ = ((EditText)getCurrent_Baseinputs_ViewPager().findViewById(R.id.editText_baseinput_bin));
					break;
				case ID_BASETYPE_DEC:
					et_ = ((EditText)getCurrent_Baseinputs_ViewPager().findViewById(R.id.editText_baseinput_dec));
					break;
				case ID_BASETYPE_HEX:
					et_ = ((EditText)getCurrent_Baseinputs_ViewPager().findViewById(R.id.editText_baseinput_hex));
					break;
				default:
					return;
			};
			
			int current_base_type = selectedBasetypeId; 
			switchBasetype(memory.base_type);
			
			/* Set a value into Base-input */
			if (et_.getText().toString().contentEquals("0000") || et_.getText().toString().contentEquals("0")) {
				et_.setText(value);
			} else {
				et_.setText(et_.getText().toString() + value);
			}
			
			baseConvert();
			
			switchBasetype(current_base_type);
			
			return;
		}


		if (et.getText().toString().contentEquals("0")) {
			if (str.contentEquals("-")) {
				et.setText(str + et.getText().toString());
			}
		} else {
			et.setText(et.getText().toString() + str);
		}
		baseConvert();
	}

	/**
	 * input equall key
	 */
	public void inputEquall() {
		calculate();
	}

	/**
	 * get current base-type(container) TableRow object
	 */
	public TableRow getCurrent_Basetype_TableRow() {
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			return (TableRow) v.findViewById(R.id.tableRow_basetype_bin);
		} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
			return (TableRow) v.findViewById(R.id.tableRow_basetype_dec);
		} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
			return (TableRow) v.findViewById(R.id.tableRow_basetype_hex);
		}
		return null;
	}

	/**
	 * get current base-type ToggleButton object
	 */
	public ToggleButton getCurrent_Basetype_ToggleButton() {
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			// Log.d("binCalc",
			// "Fragment_main - getCurrent_Basetype_ToggleButton - ID_BASETYPE_BIN");
			return (ToggleButton) v.findViewById(R.id.toggle_basetype_bin);
		} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
			// Log.d("binCalc",
			// "Fragment_main - getCurrent_Basetype_ToggleButton - ID_BASETYPE_DEC");
			return (ToggleButton) v.findViewById(R.id.toggle_basetype_dec);
		} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
			// Log.d("binCalc",
			// "Fragment_main - getCurrent_Basetype_ToggleButton - ID_BASETYPE_HEX");
			return (ToggleButton) v.findViewById(R.id.toggle_basetype_hex);
		}
		return null;
	}

	/**
	 * get current base-input(container) TableRow object
	 */
	public TableRow getCurrent_Baseinput_TableRow() {
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			return (TableRow) v.findViewById(R.id.tableRow_baseinput_bin);
		} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
			return (TableRow) v.findViewById(R.id.tableRow_baseinput_dec);
		} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
			return (TableRow) v.findViewById(R.id.tableRow_baseinput_hex);
		}
		return null;
	}

	/**
	 * get current base-input EditText object
	 */
	public EditText getCurrent_Baseinput_EditText() {
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			Log.d("binCalc",
					"Fragment_main - getCurrent_Baseinput_EditText - ID_BASETYPE_BIN");
			return (EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_bin);
		} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
			Log.d("binCalc",
					"Fragment_main - getCurrent_Baseinput_EditText - ID_BASETYPE_DEC");
			return (EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_dec);
		} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
			Log.d("binCalc",
					"Fragment_main - getCurrent_Baseinput_EditText - ID_BASETYPE_HEX");
			return (EditText) getCurrent_Baseinputs_ViewPager().findViewById(
					R.id.editText_baseinput_hex);
		}
		Log.d("binCalc",
				"Fragment_main - getCurrent_Baseinput_EditText - Default");
		return (EditText) getCurrent_Baseinputs_ViewPager().findViewById(
				R.id.editText_baseinput_bin);
	}

	/**
	 * get current base-inputs ViewPager object
	 */
	public View getCurrent_Baseinputs_ViewPager() {
		int item = baseinputsViewPager.getCurrentItem(); // History id
		Log.d("binCalc",
				"Fragment_main - getCurrent_Baseinputs_ViewPager - page = "
						+ item);
		return (View) baseinputsViewPager.findViewWithTag(item);
	}
	
	/**
	 * get current base-input Backspace (ImageView) object
	 */
	public ImageButton getCurrent_Baseinput_Backspace_ImageView() {
		if (selectedBasetypeId == ID_BASETYPE_BIN) {
			return (ImageButton) v
					.findViewById(R.id.imageView_baseinput_bs_bin);
		} else if (selectedBasetypeId == ID_BASETYPE_DEC) {
			return (ImageButton) v
					.findViewById(R.id.imageView_baseinput_bs_dec);
		} else if (selectedBasetypeId == ID_BASETYPE_HEX) {
			return (ImageButton) v
					.findViewById(R.id.imageView_baseinput_bs_hex);
		}
		return null;
	}
	
	public void updateButtonsState() {
		/* buttons for negative numbers */
		Button btn_pm = (Button)v.findViewById(R.id.keyButtonPM); // Plus/Minus button
		Button btn_minus = (Button)v.findViewById(R.id.keyButtonOpMi); // Minus button
		if (selectedBasetypeId != ID_BASETYPE_DEC) {
			// Can't use the +/- button when mode is not Decimal.
			btn_pm.setEnabled(false);
			if (selectedBasetypeId == ID_BASETYPE_BIN && getCurrent_Baseinput_EditText().getText().toString().contentEquals("0000")) {
				// Can't use the minus button when input-field has not inputted.
				btn_minus.setEnabled(false);
			} else if (selectedBasetypeId == ID_BASETYPE_HEX && getCurrent_Baseinput_EditText().getText().toString().contentEquals("0")) {
				// Can't use the minus button when input-field has not inputted.
				btn_minus.setEnabled(false);
			} else {
				btn_minus.setEnabled(true);
			}
		} else {
			btn_pm.setEnabled(true);
			btn_minus.setEnabled(true);
		}
		
		/* Memory buttons */
		CalculatorMemoryData memory =  calc.readMemory();
		Button btn_mr = (Button)v.findViewById(R.id.keyButtonMR); // Memory read button
		Button btn_mc = (Button)v.findViewById(R.id.keyButtonMC); // Memory clear button
		TextView text_mr = (TextView)v.findViewById(R.id.keyButtonMRText);
		if (memory == null) {
			text_mr.setText("");
			btn_mr.setEnabled(false);
			btn_mc.setEnabled(false);
		} else {
			text_mr.setText("("+memory.base_type+")"+memory.value);
			btn_mr.setEnabled(true);
			btn_mc.setEnabled(true);
		}
	}

	/**
	 * Switch enable key-num button by basetypeId
	 * 
	 * @param basetypeId
	 *            Base-type ID number
	 */
	public void setEnableKeyButton(int basetypeId) {
		if (basetypeId == ID_BASETYPE_HEX) {
			/* Alphabet key */
			v.findViewById(R.id.keyButtonA).setEnabled(true);
			v.findViewById(R.id.keyButtonB).setEnabled(true);
			v.findViewById(R.id.keyButtonC).setEnabled(true);
			v.findViewById(R.id.keyButtonD).setEnabled(true);
			v.findViewById(R.id.keyButtonE).setEnabled(true);
			v.findViewById(R.id.keyButtonF).setEnabled(true);
		} else {
			/* Alphabet key */
			v.findViewById(R.id.keyButtonA).setEnabled(false);
			v.findViewById(R.id.keyButtonB).setEnabled(false);
			v.findViewById(R.id.keyButtonC).setEnabled(false);
			v.findViewById(R.id.keyButtonD).setEnabled(false);
			v.findViewById(R.id.keyButtonE).setEnabled(false);
			v.findViewById(R.id.keyButtonF).setEnabled(false);
		}
		if (basetypeId == ID_BASETYPE_BIN) {
			v.findViewById(R.id.keyButton2).setEnabled(false);
			v.findViewById(R.id.keyButton3).setEnabled(false);
			v.findViewById(R.id.keyButton4).setEnabled(false);
			v.findViewById(R.id.keyButton5).setEnabled(false);
			v.findViewById(R.id.keyButton6).setEnabled(false);
			v.findViewById(R.id.keyButton7).setEnabled(false);
			v.findViewById(R.id.keyButton8).setEnabled(false);
			v.findViewById(R.id.keyButton9).setEnabled(false);
		} else {
			v.findViewById(R.id.keyButton2).setEnabled(true);
			v.findViewById(R.id.keyButton3).setEnabled(true);
			v.findViewById(R.id.keyButton4).setEnabled(true);
			v.findViewById(R.id.keyButton5).setEnabled(true);
			v.findViewById(R.id.keyButton6).setEnabled(true);
			v.findViewById(R.id.keyButton7).setEnabled(true);
			v.findViewById(R.id.keyButton8).setEnabled(true);
			v.findViewById(R.id.keyButton9).setEnabled(true);
		}
	}

	/**
	 * Switch the base-type
	 * 
	 * @param basetypeId
	 *            Base-type ID number
	 */
	public void switchBasetype(int basetypeId) {
		Log.d("binCalc", "Fragment_main - switchBasetype(" + basetypeId + ")");

		selectedBasetypeId = basetypeId;

		if (getCurrent_Baseinputs_ViewPager() == null) {
			return;
		}
		
		ToggleButton tb_type_bin = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_bin);
		ImageView bs_bin = (ImageView) v
				.findViewById(R.id.imageView_baseinput_bs_bin);
		EditText et_input_bin = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_bin);

		ToggleButton tb_type_dec = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_dec);
		ImageView bs_dec = (ImageView) v
				.findViewById(R.id.imageView_baseinput_bs_dec);
		EditText et_input_dec = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_dec);

		ToggleButton tb_type_hex = (ToggleButton) v
				.findViewById(R.id.toggle_basetype_hex);
		ImageView bs_hex = (ImageView) v
				.findViewById(R.id.imageView_baseinput_bs_hex);
		EditText et_input_hex = (EditText) getCurrent_Baseinputs_ViewPager()
				.findViewById(R.id.editText_baseinput_hex);

		/* Set enable/disable Number key buttons */
		setEnableKeyButton(basetypeId);

		/* Reset under-line (base-inputs) */
		if (bs_bin == null || et_input_bin == null) { // FIXME! fixed temporally
														// now
			return;
		}
		et_input_bin.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.edittext_baseinput_default));
		et_input_dec.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.edittext_baseinput_default));
		et_input_hex.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.edittext_baseinput_default));

		/* Reset toggle-button (base-types) */
		tb_type_bin.setChecked(false);
		tb_type_bin.setTextColor(getResources().getColor(
				R.color.main_toggle_basetype_TextColor_default));
		tb_type_dec.setChecked(false);
		tb_type_dec.setTextColor(getResources().getColor(
				R.color.main_toggle_basetype_TextColor_default));
		tb_type_hex.setChecked(false);
		tb_type_hex.setTextColor(getResources().getColor(
				R.color.main_toggle_basetype_TextColor_default));

		/* Invisible backspace-button */
		bs_bin.setImageDrawable(getResources().getDrawable(
				R.drawable.image_backspace_null));
		bs_bin.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.image_backspace_background_null));
		bs_dec.setImageDrawable(getResources().getDrawable(
				R.drawable.image_backspace_null));
		bs_dec.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.image_backspace_background_null));
		bs_hex.setImageDrawable(getResources().getDrawable(
				R.drawable.image_backspace_null));
		bs_hex.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.image_backspace_background_null));

		/* activate Base-types & base-inputs */
		getCurrent_Basetype_ToggleButton().setChecked(true);
		getCurrent_Basetype_ToggleButton().setTextColor(
				getResources().getColor(
						R.color.main_toggle_basetype_TextColor_active));
		getCurrent_Baseinput_EditText().setBackgroundDrawable(
				this.getResources().getDrawable(
						R.drawable.edittext_baseinput_active));
		getCurrent_Baseinput_Backspace_ImageView().setImageDrawable(
				getResources().getDrawable(R.drawable.image_backspace_active));
		getCurrent_Baseinput_Backspace_ImageView().setBackgroundDrawable(
				getResources().getDrawable(
						R.drawable.image_backspace_background_active));
		
		updateButtonsState();
	}

	/**
	 * Re-apply the base-type
	 */
	public void reApplyBasetype() {
		switchBasetype(selectedBasetypeId);
	}
	
	/**
	 * Restore base-inputs from from a history
	 */
	public boolean restoreBaseInputsFromHistory(int history_id) {
		HistoryItem history = calc.getHistory(history_id);
		if (history == null) {
			Log.e("binCalc",
					"Fragment_main - restoreBaseInputsFromHistory - History not found");

			getCurrent_Baseinput_EditText().setText("0");
			return false;
		}
		
		Log.d("binCalc",
			"Fragment_main - restoreBaseInputsFromHistory - Restore a history("
					+ history_id + ") = " + history.getNumberString());
		switchBasetype(history.getBaseType());
		getCurrent_Baseinput_EditText().setText(history.getNumberString());
		
		baseConvert();
		reApplyBasetype();
		return true;
	}

	/* Event-handler for Parent activity has created */
	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		Log.i("binCalc", "Fragment_main - onActivityCreated");
	}

	/* Event-handler for onStart */
	@Override
	public void onStart() {
		super.onStart();
	}

	/* Event-handler for buttons */
	@Override
	public void onClick(View v) {
		if (prefKeyVibration) {
			vib.vibrate(DEFAULT_VIBRATION_MSEC);
		}
		switch (v.getId()) {
		/* Backspace button */
		case R.id.imageButton_baseinput_backspace:
			inputBackspace();
			break;

		/* Key-buttons (0-9) */
		case R.id.keyButton0:
			inputBasenumber("0");
			break;
		case R.id.keyButton1:
			inputBasenumber("1");
			break;
		case R.id.keyButton2:
			inputBasenumber("2");
			break;
		case R.id.keyButton3:
			inputBasenumber("3");
			break;
		case R.id.keyButton4:
			inputBasenumber("4");
			break;
		case R.id.keyButton5:
			inputBasenumber("5");
			break;
		case R.id.keyButton6:
			inputBasenumber("6");
			break;
		case R.id.keyButton7:
			inputBasenumber("7");
			break;
		case R.id.keyButton8:
			inputBasenumber("8");
			break;
		case R.id.keyButton9:
			inputBasenumber("9");
			break;
		case R.id.keyButtonA:
			inputBasenumber("A");
			break;
		case R.id.keyButtonB:
			inputBasenumber("B");
			break;
		case R.id.keyButtonC:
			inputBasenumber("C");
			break;
		case R.id.keyButtonD:
			inputBasenumber("D");
			break;
		case R.id.keyButtonE:
			inputBasenumber("E");
			break;
		case R.id.keyButtonF:
			inputBasenumber("F");
			break;
		case R.id.keyButtonPo:
			inputBasenumber(".");
			break;

		/* operator-button */
		case R.id.keyButtonOpPl:
			inputOpr(ID_OPRMODE_PLUS);
			break;
		case R.id.keyButtonOpMi:
			inputOpr(ID_OPRMODE_MINUS);
			break;
		case R.id.keyButtonOpMp:
			inputOpr(ID_OPRMODE_MULTI);
			break;
		case R.id.keyButtonOpDi:
			inputOpr(ID_OPRMODE_DIVIS);
			break;
		
		/* Memory button */
		case R.id.keyButtonMin:
			inputOpr(ID_OPRMODE_MEMORY_IN);
			break;
		case R.id.keyButtonMC:
			inputOpr(ID_OPRMODE_MEMORY_CLEAR);
			break;
		case R.id.keyButtonMR:
			inputOpr(ID_OPRMODE_MEMORY_READ);
			break;

		/* Equall-button */
		case R.id.keyButtonEq:
			inputEquall();
			break;
		}
		;
	}

	/* Event-handler for buttons (Long-click) */
	@Override
	public boolean onLongClick(View v) {
		if (prefKeyVibration) {
			vib.vibrate(DEFAULT_VIBRATION_MSEC);
		}
		switch (v.getId()) {
		/* Backspace button => Long-click ... All backspace */
		case R.id.imageButton_baseinput_backspace:
			inputClear();
			return false;
		}
		;
		return true;
	}

	/* Event-handler for Viewpager */
	private class PageListener extends SimpleOnPageChangeListener {
		public void onPageSelected(int position) {
			Log.d("binCalc",
					"Fragment_main - PageListener - onPageSelected - Position = "
							+ position);

			/* Restore for when change to before pages */
			restoreBaseInputsFromHistory(position);
		}
	}

	/*
	 * Initialize process (It has call when ViewPager has just completed a
	 * instantiateItem() method.)
	 */
	public void init() {
		if (is_init == false) { // If not yet initialized
			Log.d("binCalc", "Fragment_main - init()");
			is_init = true;
			inputClear();

			/* Load default value */
			switchBasetype(selectedBasetypeId);
			if (defaultValue != null) {
				getCurrent_Baseinput_EditText().setText(defaultValue);
				baseConvert();
			}

			/* Save initialized calculator, into histories list */
			HistoryItem history = new HistoryItem();
			history.setBaseType(selectedBasetypeId);
			history.setNumberString(getCurrent_Baseinput_EditText().getText().toString());
			int history_id = calc.putHistory(history);
			Log.d("binCalc", "calculate() - Save a history(" + history_id
					+ ") = " + history.getNumberString());
			
			/* Initialize buttons state */
			updateButtonsState();
		}
	}
}
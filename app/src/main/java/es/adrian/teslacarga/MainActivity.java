package es.adrian.teslacarga;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.text.InputType;
import android.widget.*;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(16,16,16), CARD = Color.rgb(32,32,32), WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(175,175,175), RED = Color.rgb(232,33,39), GREEN = Color.rgb(87,190,130);
    private EditText batteryInput, ampsInput, targetInput;
    private TextView ampsLabel, result80, result100, finish80, finish100, powerLabel;
    private TextView departureTime, countdownResult, countdownDetails;
    private int departureHour = 7, departureMinute = 0;
    private SeekBar ampsSeek;
    private final DecimalFormat df = new DecimalFormat("0.0");

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(22),dp(20),dp(22),dp(28));
        scroll.addView(root); setContentView(scroll);

        TextView brand = text("TESLA · MODEL 3", 13, RED, true); brand.setLetterSpacing(.12f); root.addView(brand);
        TextView title = text("Calculadora de carga", 28, WHITE, true); LinearLayout.LayoutParams tp = params(-1,-2); tp.topMargin=dp(8); root.addView(title,tp);
        TextView subtitle = text("Tracción trasera · batería LFP · carga monofásica", 13, MUTED, false); tp=params(-1,-2);tp.topMargin=dp(5);root.addView(subtitle,tp);

        LinearLayout batteryCard = card(root); addLabel(batteryCard,"BATERÍA ACTUAL");
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); batteryCard.addView(row);
        batteryInput = input("52", "%"); row.addView(batteryInput,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView percent = text("%",22, MUTED,true); LinearLayout.LayoutParams pp=params(-2,-2);pp.leftMargin=dp(12);row.addView(percent,pp);

        LinearLayout ampsCard=card(root); addLabel(ampsCard,"INTENSIDAD DE CARGA");
        ampsLabel=text("16 A",23,WHITE,true); ampsCard.addView(ampsLabel);
        ampsSeek=new SeekBar(this); ampsSeek.setMax(25); ampsSeek.setProgress(16); ampsSeek.setProgressTintList(ColorStateList.valueOf(RED)); ampsSeek.setThumbTintList(ColorStateList.valueOf(RED));
        LinearLayout.LayoutParams sp=params(-1,-2);sp.topMargin=dp(6);ampsCard.addView(ampsSeek,sp);
        LinearLayout range=new LinearLayout(this);range.setGravity(Gravity.CENTER_VERTICAL);range.setOrientation(LinearLayout.HORIZONTAL);ampsCard.addView(range);
        TextView min=text("1 A",12,MUTED,false), max=text("26 A",12,MUTED,false);range.addView(min,new LinearLayout.LayoutParams(0,-2,1));range.addView(max);
        TextView manual=text("O introduce los amperios manualmente",13,MUTED,false);sp=params(-1,-2);sp.topMargin=dp(16);ampsCard.addView(manual,sp);
        ampsInput=input("16","A"); sp=params(-1,dp(50));sp.topMargin=dp(7);ampsCard.addView(ampsInput,sp);
        ampsSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){int a=p+1;ampsLabel.setText(a+" A");if(user)ampsInput.setText(String.valueOf(a));calculate();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});
        ampsInput.setOnFocusChangeListener((v,has)->{if(!has)syncManualAmps();});
        ampsInput.setOnEditorActionListener((v,action,event)->{syncManualAmps();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(ampsInput.getWindowToken(),0);ampsInput.clearFocus();return true;});
        batteryInput.setOnFocusChangeListener((v,has)->{if(!has)calculate();});
        batteryInput.setOnEditorActionListener((v,action,event)->{calculate();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(batteryInput.getWindowToken(),0);batteryInput.clearFocus();return true;});

        LinearLayout results=card(root); addLabel(results,"TIEMPO ESTIMADO");
        addResultRow(results,"Hasta el 80 %", "80", true);
        View divider=new View(this);divider.setBackgroundColor(Color.rgb(60,60,60));LinearLayout.LayoutParams dpv=params(-1,1);dpv.topMargin=dp(16);dpv.bottomMargin=dp(16);results.addView(divider,dpv);
        addResultRow(results,"Hasta el 100 %", "100", false);
        LinearLayout planCard=card(root); addLabel(planCard,"CUENTA ATRÁS PARA SALIR");
        TextView planIntro=text("Indica a qué hora sales y con qué porcentaje quieres salir. Calcularemos la hora límite para conectar el coche.",13,MUTED,false);
        planCard.addView(planIntro);
        TextView targetLabel=text("BATERÍA OBJETIVO (%)",11,MUTED,true); tp=params(-1,-2);tp.topMargin=dp(15);planCard.addView(targetLabel,tp);
        targetInput=input("80","%");
        tp=params(-1,dp(50));tp.topMargin=dp(7);planCard.addView(targetInput,tp);
        TextView departureLabel=text("HORA DE SALIDA",11,MUTED,true);tp=params(-1,-2);tp.topMargin=dp(15);planCard.addView(departureLabel,tp);
        departureTime=text("07:00  ·  Cambiar hora",17,WHITE,true);departureTime.setGravity(Gravity.CENTER);
        departureTime.setPadding(dp(12),dp(13),dp(12),dp(13));departureTime.setBackground(round(Color.rgb(45,45,45),dp(10)));
        tp=params(-1,-2);tp.topMargin=dp(7);planCard.addView(departureTime,tp);
        departureTime.setOnClickListener(v->new TimePickerDialog(this,(view,hour,minute)->{departureHour=hour;departureMinute=minute;updateDepartureLabel();calculateCountdown();},departureHour,departureMinute,true).show());
        TextView calcPlan=text("CALCULAR HORA LÍMITE",13,RED,true);calcPlan.setGravity(Gravity.CENTER);
        tp=params(-1,dp(46));tp.topMargin=dp(8);planCard.addView(calcPlan,tp);
        calcPlan.setOnClickListener(v->{calculate();calculateCountdown();});
        countdownResult=text("Introduce los datos para calcular",20,GREEN,true);tp=params(-1,-2);tp.topMargin=dp(12);planCard.addView(countdownResult,tp);
        countdownDetails=text("",13,MUTED,false);tp=params(-1,-2);tp.topMargin=dp(6);planCard.addView(countdownDetails,tp);
        targetInput.setOnFocusChangeListener((v,has)->{if(!has)calculateCountdown();});
        targetInput.setOnEditorActionListener((v,action,event)->{calculateCountdown();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(targetInput.getWindowToken(),0);targetInput.clearFocus();return true;});
        updateDepartureLabel();

        powerLabel=text("Potencia aproximada: 3,68 kW",13,MUTED,false);tp=params(-1,-2);tp.topMargin=dp(14);root.addView(powerLabel,tp);
        TextView note=text("Estimación orientativa basada en 230 V, batería útil estimada de 60 kWh y 90 % de eficiencia. El tiempo real puede variar por temperatura, pérdidas y gestión de carga del vehículo.",12,MUTED,false);tp=params(-1,-2);tp.topMargin=dp(18);root.addView(note,tp);
        TextView reset=text("RESTABLECER EJEMPLO",12,RED,true);reset.setGravity(Gravity.CENTER);tp=params(-1,dp(48));tp.topMargin=dp(18);root.addView(reset,tp);reset.setOnClickListener(v->{batteryInput.setText("52");ampsInput.setText("16");ampsSeek.setProgress(15);calculate();});
        calculate();
    }

    private void addResultRow(LinearLayout parent,String label,String target,boolean first){
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);parent.addView(row);
        TextView l=text(label,15,MUTED,false);row.addView(l);
        TextView r=text("—",29,first?GREEN:WHITE,true);LinearLayout.LayoutParams p=params(-1,-2);p.topMargin=dp(5);row.addView(r,p);
        TextView f=text("",12,MUTED,false);p=params(-1,-2);p.topMargin=dp(3);row.addView(f,p);
        if(first){result80=r;finish80=f;}else{result100=r;finish100=f;}
    }
    private LinearLayout card(LinearLayout parent){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(17),dp(17),dp(17),dp(17));c.setBackground(round(CARD,dp(18)));LinearLayout.LayoutParams p=params(-1,-2);p.topMargin=dp(18);parent.addView(c,p);return c;}
    private void addLabel(LinearLayout c,String s){TextView t=text(s,11,MUTED,true);t.setLetterSpacing(.1f);LinearLayout.LayoutParams p=params(-1,-2);p.bottomMargin=dp(11);c.addView(t,p);}
    private EditText input(String value,String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setTextColor(WHITE);e.setHintTextColor(MUTED);e.setTextSize(20);e.setSelectAllOnFocus(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setPadding(dp(12),0,dp(12),0);e.setBackground(round(Color.rgb(45,45,45),dp(10)));return e;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private android.graphics.drawable.GradientDrawable round(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    private LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private void syncManualAmps(){try{int a=Integer.parseInt(ampsInput.getText().toString().trim());if(a<1)a=1;if(a>26)a=26;ampsInput.setText(String.valueOf(a));ampsSeek.setProgress(a-1);ampsLabel.setText(a+" A");}catch(Exception e){ampsInput.setText(String.valueOf(ampsSeek.getProgress()+1));}calculate();}
    private void calculate(){
        if(batteryInput==null||ampsInput==null||result80==null)return;
        double soc;int amps;
        try{soc=Double.parseDouble(batteryInput.getText().toString().trim());}catch(Exception e){showInvalid();return;}
        try{amps=Integer.parseInt(ampsInput.getText().toString().trim());}catch(Exception e){amps=ampsSeek.getProgress()+1;}
        if(soc<0||soc>100||amps<1||amps>26){showInvalid();return;}
        double power=230.0*amps/1000.0; double effective=power*.90; double capacity=60.0;
        double hours80=Math.max(0,(Math.max(0,80-soc)/100.0*capacity)/effective);
        double hours100=Math.max(0,((100-soc)/100.0*capacity)/effective);
        // Small allowance for the final stage near full charge.
        if(soc<100) hours100 += Math.max(0,Math.min(1.0,(100-Math.max(soc,90))/10.0))*.12;
        result80.setText(soc>=80?"Ya está al 80 %":""+formatDuration(hours80));
        result100.setText(soc>=100?"Ya está al 100 %":formatDuration(hours100));
        finish80.setText(soc>=80?"No necesita más carga":"Energía aproximada: "+df.format(Math.max(0,80-soc)/100.0*capacity)+" kWh · " + finishTime(hours80));
        finish100.setText(soc>=100?"Batería completa":"Energía aproximada: "+df.format((100-soc)/100.0*capacity)+" kWh · " + finishTime(hours100));
        powerLabel.setText("Potencia de red: "+df.format(power)+" kW · Potencia estimada a batería: "+df.format(effective)+" kW");
        calculateCountdown();
    }
    private void updateDepartureLabel(){
        departureTime.setText(String.format(java.util.Locale.getDefault(),"%02d:%02d  ·  Cambiar hora",departureHour,departureMinute));
    }
    private void calculateCountdown(){
        if(targetInput==null||countdownResult==null||batteryInput==null||ampsInput==null)return;
        double current,target; int amps;
        try{current=Double.parseDouble(batteryInput.getText().toString().trim());target=Double.parseDouble(targetInput.getText().toString().trim());amps=Integer.parseInt(ampsInput.getText().toString().trim());}
        catch(Exception e){countdownResult.setText("Revisa los datos");countdownDetails.setText("Introduce la batería actual, el objetivo y los amperios.");return;}
        if(current<0||current>100||target<0||target>100||amps<1||amps>26){
            countdownResult.setText("Revisa los datos");countdownDetails.setText("La batería debe estar entre 0 y 100 % y los amperios entre 1 y 26.");return;
        }
        if(target<=current){
            countdownResult.setText("Ya tienes batería suficiente");
            countdownDetails.setText("Tu batería actual ("+df.format(current)+" %) alcanza el objetivo de "+df.format(target)+" %.");
            return;
        }
        double effective=(230.0*amps/1000.0)*.90;
        double hours=((target-current)/100.0*60.0)/effective;
        if(target>90)hours+=Math.max(0,Math.min(1.0,(target-Math.max(current,90))/10.0))*.12;
        java.util.Calendar now=java.util.Calendar.getInstance();
        java.util.Calendar departure=(java.util.Calendar)now.clone();
        departure.set(java.util.Calendar.HOUR_OF_DAY,departureHour);
        departure.set(java.util.Calendar.MINUTE,departureMinute);
        departure.set(java.util.Calendar.SECOND,0);departure.set(java.util.Calendar.MILLISECOND,0);
        if(!departure.after(now))departure.add(java.util.Calendar.DAY_OF_YEAR,1);
        java.util.Calendar latest=(java.util.Calendar)departure.clone();
        latest.add(java.util.Calendar.MINUTE,-(int)Math.ceil(hours*60.0));
        String duration=formatDuration(hours);
        java.text.SimpleDateFormat timeFormat=new java.text.SimpleDateFormat("HH:mm",java.util.Locale.getDefault());
        java.util.Calendar today=(java.util.Calendar)now.clone();
        today.set(java.util.Calendar.HOUR_OF_DAY,0);today.set(java.util.Calendar.MINUTE,0);today.set(java.util.Calendar.SECOND,0);today.set(java.util.Calendar.MILLISECOND,0);
        java.util.Calendar latestDay=(java.util.Calendar)latest.clone();
        latestDay.set(java.util.Calendar.HOUR_OF_DAY,0);latestDay.set(java.util.Calendar.MINUTE,0);latestDay.set(java.util.Calendar.SECOND,0);latestDay.set(java.util.Calendar.MILLISECOND,0);
        long dayDiff=(latestDay.getTimeInMillis()-today.getTimeInMillis())/86400000L;
        String dayLabel=dayDiff<=0?"hoy":(dayDiff==1?"mañana":"el "+new java.text.SimpleDateFormat("dd/MM",java.util.Locale.getDefault()).format(latest.getTime()));
        if(latest.before(now)){
            countdownResult.setText("Conecta el coche ahora");
            long lateMinutes=Math.max(0,(now.getTimeInMillis()-latest.getTimeInMillis())/60000L);
            countdownDetails.setText("Necesitas aproximadamente "+duration+" para llegar al "+df.format(target)+" %. La hora límite ya ha pasado por "+formatDuration(lateMinutes/60.0)+". Si sales a las "+timeFormat.format(departure.getTime())+", podría no dar tiempo.");
        }else{
            countdownResult.setText("Conecta antes de las "+timeFormat.format(latest.getTime()));
            countdownDetails.setText("Hora límite: "+dayLabel+" a las "+timeFormat.format(latest.getTime())+". Necesitas "+duration+" de carga para pasar del "+df.format(current)+" % al "+df.format(target)+" %. Salida prevista: "+timeFormat.format(departure.getTime())+".");
        }
    }
    private String formatDuration(double hours){int total=(int)Math.round(hours*60);int h=total/60,m=total%60;if(h==0)return m+" min";if(m==0)return h+" h";return h+" h "+m+" min";}
    private String finishTime(double hours){java.util.Calendar c=java.util.Calendar.getInstance();c.add(java.util.Calendar.MINUTE,(int)Math.round(hours*60));return String.format(java.util.Locale.getDefault(),"Terminaría aprox. a las %02d:%02d",c.get(java.util.Calendar.HOUR_OF_DAY),c.get(java.util.Calendar.MINUTE));}
    private void showInvalid(){result80.setText("Revisa los datos");result100.setText("Revisa los datos");finish80.setText("Batería: introduce un valor entre 0 y 100.");finish100.setText("Intensidad: introduce un valor entre 1 y 26 A.");}
}

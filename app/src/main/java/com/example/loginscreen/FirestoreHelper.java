package com.example.loginscreen;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreHelper {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void addTestData() {
        TouristObject object1 = new TouristObject("Античен град 'Хераклея Синтика'", "Античният град Хераклея Синтика се намира близо до Петрич...", "БЛАГОЕВГРАДСКА ОБЛАСТ/Петрич", "antichengrad");
        TouristObject object2 = new TouristObject("Музей Рилски манастир", "Рилският манастир е основан през X век от св. Иван Рилски и е най-големият духовен център в България. През XIV век феодалът Хрельо Драговол го укрепява, а Хрельовата кула става емблематична. Главната църква „Рождество Богородично“ е изографисана от Захари Зограф и други майстори. Манастирът притежава богата библиотека и музей с 35 000 исторически експоната. Уникални реликви са чудотворната икона на Света Богородица Одигитрия и изящно резбованият кръст на монах Рафаил, който ослепява след 12 години работа върху него. ", "КЮСТЕНДИЛСКА ОБЛАСТ", "rilski");
        TouristObject object3 = new TouristObject("Поморийско езеро","Поморийско езеро е свръхсолена лагуна на българското Черноморско крайбрежие, близо до гр. Поморие. То е дом на редки соленолюбиви растения и животни, а също така е част от миграционния път Via Pontica. В езерото се наблюдават над 270 вида птици през различните сезони.Зоната е защитена по българското и международното природозащитно законодателство, включена в Рамсарската конвенция и Натура 2000.Посетителският център на Поморийско езеро предлага екскурзии, наблюдение на птици и изложби, с цел популяризиране на биоразнообразието и природозащитни нагласи. От терасата се открива гледка към езерото и гнездовата колония на гривестите рибарки.", "БУРГАСКА ОБЛАСТ/Поморие", "pomoriiskoezero");
        TouristObject object4 = new TouristObject("Царевец","Царевград Търнов е бил столица на българската държава през XII–XIV век и един от най-могъщите градове в Югоизточна Европа. Център на управлението бил хълмът Царевец с царския дворец и Патриаршеския комплекс. В двореца живеели владетелите, а в патриаршията се ръководел духовният живот. Днес може да се посети реставрираният храм „Възнесение Господне“, украсен с мащабни стенописи. Археолозите са разкрили над 470 жилища, 23 храма и 4 манастира, доказващи значимостта и развитието на града.", "Велико Търново", "tsarevets");
        TouristObject object5 = new TouristObject("Планетариум","Планетариумът, открит през 1975 г., е водеща туристическа атракция с над 3 милиона посетители и хиляди звездни сеанси и телескопични наблюдения. Днес впечатлява с модерен дигитален купол с диаметър 12.5 м и система Digistar7, позволяваща реалистични космически прожекции. Обсерваторията разполага с мощен телескоп Celestron, а при ясно време се провеждат демонстрации за посетители. Новият посетителски център предлага макет на Луната, интерактивна Слънчева система и експонати за съзвездията и светлината.", "Смолян", "planetarium");
        TouristObject object6 = new TouristObject("Пещера Ухловица","Пещера Ухловица се намира в Западните Родопи на 1040 м надморска височина, близо до село Могилица. Тя е една от най-старите и красиви пещери в района, известна с дендритните си образувания, каскадни езера и скални водопади. Температурата в нея е постоянна – 10–11°C. Най-впечатляваща е галерията на долния етаж, до която се стига по стръмна метална стълба. Пещерата е частично благоустроена и достъпна за туристи." , "СМОЛЯНСКА ОБЛАСТ/Родопи", "peshtera");
        TouristObject object7 = new TouristObject("Ягодинска пещера","Ягодинската пещера е третата по дължина в България (10 500 м) и най-дългата в Родопите. Образувана преди около 275 000 години, тя е разположена на три основни нива, като туристическият маршрут е 1100 м и включва впечатляващи образувания като сталактити, сталактони, драперии, дендрити и пещерни перли. В пещерата е открито запазено енеолитно жилище от IV хил. пр. Хр. с уникални находки. Температурата е постоянна – 6°C, влажност 85–91%. Обитават я прилепи, троглобионти и пещерни скакалци, а във влажните участъци се срещат лишеи и гъби. В специална зала се провеждат и сватбени ритуали.", "СМОЛЯНСКА ОБЛАСТ/Родопи", "yagodinska");
        TouristObject object8 = new TouristObject("вр. Мусала (2925 м.)","Рила е най-високата планина в България и на Балканския полуостров, с връх Мусала (2925 м). Тя се дели на четири дяла, като в нея се намират Седемте рилски езера, Леденото и Смрадливото езеро, и извиращи реки като Искър, Марица и Места. Рила е богата на езера (около 170) и природни резервати. Мусала впечатлява с гранитния си силует и седемте мусаленски езера под него. До върха води маршрут от Боровец през х. Мусала, като там се намира и най-високата метеорологична станция в Югоизточна Европа.", "СОФИЙСКА ОБЛАСТ", "vmusala");
        TouristObject object9 = new TouristObject("Перперикон","Перперикон се намира на 15 км от Кърджали и се издига на 470 м надморска височина. Смята се, че това е прочутото тракийско светилище на бог Дионис, споменато от Херодот и Светоний. Според археолога проф. Овчаров, Перперикон е възникнал в края на бронзовата епоха и бил важен култов и политически център до ранното християнство. Най-значими са дворецът-светилище и откритата ранна християнска църква. Комплексът остава важен и през Средновековието, а разкопките продължават да разкриват нови открития.", "КЪРДЖАЛИЙСКА ОБЛАСТ/Кърджали", "perperikon");
        TouristObject object10 = new TouristObject("Монумент Света Богородица","В Хасково се издига най-високата в света статуя на Дева Мария с Младенеца – висока 32,8 м и тежаща 120 тона, открита през 2003 г. и вписана в Книгата на рекордите на Гинес през 2005 г. В основата ѝ се намира параклис „Рождество Богородично“. Статуята е символ на града и израз на почит към Божията Майка, считана за негов покровител. Изградена е изцяло с дарения, а 8 септември – празникът Рождество Богородично – е обявен за Ден на Хасково.", "ХАСКОВСКА ОБЛАСТ/Хасково", "bogoroditsa");

        firestore.collection("TouristObjects").document(object1.getName()).set(object1);
        firestore.collection("TouristObjects").document(object2.getName()).set(object2);
        firestore.collection("TouristObjects").document(object3.getName()).set(object3);
        firestore.collection("TouristObjects").document(object4.getName()).set(object4);
        firestore.collection("TouristObjects").document(object5.getName()).set(object5);
        firestore.collection("TouristObjects").document(object6.getName()).set(object6);
        firestore.collection("TouristObjects").document(object7.getName()).set(object7);
        firestore.collection("TouristObjects").document(object8.getName()).set(object8);
        firestore.collection("TouristObjects").document(object9.getName()).set(object9);
        firestore.collection("TouristObjects").document(object10.getName()).set(object10);
    }

    public void addToBeenThere(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в BeenThere!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в BeenThere", Toast.LENGTH_SHORT).show()
                );
    }


    public void loadVisitedObjects(Context context, OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("beenThere")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TouristObject> visitedList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        TouristObject obj = doc.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(doc.getId()); // ако използваш името за ID
                            visitedList.add(obj);
                        }
                    }
                    listener.onDataLoaded(visitedList);
                })
                .addOnFailureListener(e -> listener.onDataFailed(e.getMessage()));
    }


    public void loadTouristObjectDetails(String objectName, final OnDataLoadedListener listener) {
        firestore.collection("TouristObjects")
                .document(objectName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        TouristObject touristObject = task.getResult().toObject(TouristObject.class);
                        listener.onDataLoaded(touristObject);
                    } else {
                        listener.onDataFailed("Няма намерени данни за обект " + objectName);
                    }
                });
    }

    public void removeObjectFromBeenThere(Context context, TouristObject object, final OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("beenThere")
                .document(object.getName())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Обектът е премахнат от BeenThere!", Toast.LENGTH_SHORT).show();
                    listener.onDataLoaded(new ArrayList<>());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Грешка при премахване от BeenThere!", Toast.LENGTH_SHORT).show();
                    listener.onDataFailed("Грешка при премахване на обект от BeenThere");
                });

    }

    public void addToWishlist(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в Wishlist!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в Wishlist!", Toast.LENGTH_SHORT).show()
                );
    }

    public void addToPlan(Context context, TouristObject object) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .document(object.getName())
                .set(object)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Добавено в Plan!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Грешка при добавяне в Plan!", Toast.LENGTH_SHORT).show()
                );
    }

    public void loadPlanObjects(Context context, OnDataLoadedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("plan")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TouristObject> planList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        TouristObject obj = doc.toObject(TouristObject.class);
                        if (obj != null) {
                            obj.setName(doc.getId());
                            planList.add(obj);
                        }
                    }
                    listener.onDataLoaded(planList);
                })
                .addOnFailureListener(e -> listener.onDataFailed(e.getMessage()));
    }



    public interface OnDataLoadedListener {
        void onDataLoaded(List<TouristObject> touristObjects);
        void onDataFailed(String errorMessage);
        void onDataLoaded(TouristObject touristObject);
    }
}

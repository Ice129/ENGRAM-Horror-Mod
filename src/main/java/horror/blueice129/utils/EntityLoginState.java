package horror.blueice129.utils;

import horror.blueice129.data.HorrorModPersistentState;

public class EntityLoginState {
    private static final String ENTITY_ONLINE_KEY = "entityOnline";

    public static boolean isEntityOnline(HorrorModPersistentState state){
        int entityOnline = state.getIntValue(ENTITY_ONLINE_KEY, -1);
        if (entityOnline == -1) {
            state.setIntValue(ENTITY_ONLINE_KEY, 0);
            return false;
        }
        return entityOnline == 1;
    }
}

package origen.test_methods;

import origen.common.OrigenDeviceData;

public class ClearOrigenDeviceData extends Base {

    @Override
    public void execute() {
        OrigenDeviceData.clearStorage();
    }

}

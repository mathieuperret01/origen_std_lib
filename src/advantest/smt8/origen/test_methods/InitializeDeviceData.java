package origen.test_methods;

import origen.common.OrigenDeviceData;

public class InitializeDeviceData extends Base {

    @Override
    public void execute() {
        OrigenDeviceData.clearStorage();

        OrigenDeviceData devData = OrigenDeviceData.getInstance(this);
    }

}

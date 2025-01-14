package origen.test_methods;

import origen.common.Origen;
import origen.common.OrigenHelpers;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteDoubleArray;
import xoc.dta.resultaccess.IDcVIResults;
import xoc.dta.resultaccess.IDcVIResults.IImeasResults;
import xoc.dta.resultaccess.IDcVIResults.IVmeasResults;
import xoc.dta.resultaccess.IMeasurementResult;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/** An example test method using test descriptor */
public class DC_Measurement extends Base {

  public IFunctionalTestDescriptor FUNC;
  public IParametricTestDescriptor PAR;

  public enum MEAS {
    CURR,
    VOLT
  }

  MultiSiteDouble _result;

  boolean _applyShutdown;
  String _shutdownPattern;
  int _checkShutdown;
  MEAS _measure;
  double _settlingTime;
  String _pin;
  double _forceValue;
  double _iRange;
  double _VclampLow;
  double _VclampHigh;
  boolean _highAccuracy;
  double _limitsLow;
  double _limitsHigh;
  int _averages;

  int _badc;
  String _actionName;
  String _actionForce;
  public DC_Measurement origen;

  /** variable to catch fail if app doesn't defined */
  protected String prefixDSAgen;

  static OrigenHelpers origenHelpers;

  /** The result of executing the primary pattern */
  private IMeasurementResult funcResult;

  @Override
  public void measure_setup() {
    logTrace("DC_Measurement", "measure_setup");
  }

  public DC_Measurement applyShutdown(boolean v) {
    _applyShutdown = v;
    return this;
  }

  public DC_Measurement shutdownPattern(String v) {
    _shutdownPattern = v;
    return this;
  }

  public DC_Measurement checkShutdown(int v) {
    _checkShutdown = v;
    return this;
  }

  public DC_Measurement measure(MEAS volt) {
    _measure = volt;
    return this;
  }

  public DC_Measurement settlingTime(double v) {
    _settlingTime = v;
    return this;
  }

  public DC_Measurement averages(int v) {
    _averages = v;
    return this;
  }

  public DC_Measurement VclampLow(double v) {
    _VclampLow = v;
    return this;
  }

  public DC_Measurement VclampHigh(double v) {
    _VclampHigh = v;
    return this;
  }

  public DC_Measurement highAccuracy(boolean v) {
    _highAccuracy = v;
    return this;
  }

  public DC_Measurement limitsLow(double v) {
    _limitsLow = v;
    return this;
  }

  public DC_Measurement limitsHigh(double v) {
    _VclampHigh = v;
    return this;
  }

  public DC_Measurement pin(String v) {
    _pin = v;
    return this;
  }

  public DC_Measurement forceValue(double v) {
    _forceValue = v;
    return this;
  }

  public DC_Measurement iRange(double v) {
    _iRange = v;
    return this;
  }

  public DC_Measurement badc(int v) {
    _badc = v;
    return this;
  }

  public MultiSiteDouble result() {
    return _result;
  }

  @Override
  public void _setup() {
    //prefixDSAgen = context.testProgram().variables().getString("OPERATION_TYPE").get();
    message(10, "DC_Measurement --> _Setup");
    origen = this;
    pin("NVM_ANALOGIO");
    applyShutdown(true);
    checkShutdown(1);
    settlingTime(1000 * 1e-6);
    forceValue(0);
    iRange(0);
    VclampLow(0);
    VclampHigh(3);
    highAccuracy(true);
    limitsHigh(3);
    limitsLow(0);
    averages(1);
    badc(0);
    shutdownPattern("");

    // Call setup from the testmethod
    measure_setup();

    message(Origen.LOG_METHODTRACE, "Setting up " + _measure + " measurement");

    if (_measure == MEAS.VOLT) {
      _actionName = "Vmeas";
      _actionForce = "IForce";
    } else {
      _actionName = "Imeas";
      _actionForce = "vForce";
    }

    if (_applyShutdown) {
      if (_shutdownPattern.isEmpty()) {
          message(
                  Origen.LOG_FAIL,
                  "Shutdown pattern not provide, NOT using a shutdown pattern!");
              applyShutdown(false);
              shutdownPattern("");
      }
    }

    // Create an action sequence for the measurement
    IDeviceSetup ds = DeviceSetupFactory.createInstance(); //prefixDSAgen);
    ds.importSpec(measurement.getSpecificationName());

    //        ISetupDigInOut dcVISetup = ds.addDigInOut("NVM_ANA_PIN");
    //        forceValue = I_force; // The forced current
    //        waitTime = 1000us; // A delay between forcing the current and measuring the voltage
    //        vclampHigh = 3V; // The upper-limit of the voltage clamp
    //        vclampLow = 0.0V; // The lower-limit of the voltage clamp
    //        highAccuracy = true; // The highAccuracy mode of the digInOut instrument is turned off
    // in this example
    //        limits.low = 0V; // The lower-limit for the measurment to pass
    //        limits.high = 3V; // The upper-limit for the measurement to pass
    //
    //
    //        IIforceVmeas iforceVmeas = dcVISetup.iforceVmeas("iforceVmeas");
    //        iforceVmeas.setForceValue(0);
    //        iforceVmeas.setVclampHigh(1);
    //        iforceVmeas.setVclampLow(-1);
    //        iforceVmeas.setAverages(_averages);
    //        iforceVmeas.setWaitTime(_settlingTime)
    //
    //        action iforceVmeas iforceVmeas {
    //            forceValue = I_force; // The forced current
    //            waitTime = 1000us; // A delay between forcing the current and measuring the
    // voltage
    //            vclampHigh = 3V; // The upper-limit of the voltage clamp
    //            vclampLow = 0.0V; // The lower-limit of the voltage clamp
    //            highAccuracy = true; // The highAccuracy mode of the digInOut instrument is turned
    // off in this example
    //            limits.low = 0V; // The lower-limit for the measurment to pass
    //            limits.high = 3V; // The upper-limit for the measurement to pass
    //
    //        }
    //        action vforceImeas vforceImeas {
    //            forceValue = V_force; // The forced voltage
    //            waitTime = 1.5ms; // A delay between forcing the voltage and measuring the current
    //            irange = 0.1uA; // The expected range of the measured current
    //            highAccuracy = false; // The highAccuracy mode of the digInOut instrument is
    // turned off in this example
    //            limits.low = -20.0nA; // The lower-limit for the measurment to pass
    //            limits.high = -5.0nA; // The upper-limit for the measurement to pass
    //
    //        }

    // ds.parallelBegin("MeasurementSequence");
    // {

    if (_measure == MEAS.CURR)
    {
        ds.addDcVI(_pin).setDisconnect(true).setDisconnectModeHiz()
            .vforce(_actionForce).setForceValue(_forceValue).setIclamp("100uA");
    ds.addDcVI(_pin).imeas(_actionName).setIrange(_iRange);
    }

    else{
            {ds.addDcVI(_pin).setDisconnect(true).setDisconnectModeHiz()
                    .iforce(_actionForce).setForceValue(_forceValue).setVclampHigh(_VclampHigh).setVclampLow(_VclampLow); // investigate i clamp.setIclamp("0.1uA");
          ds.addDcVI(_pin).vmeas(_actionName);
            }
    }

    ds.sequentialBegin("PatAndShutdownSeq");
    {
        ds.parallelBegin();
      ds.patternCall(measurement.getPatternName());
      ds.parallelEnd();
      ds.parallelBegin();

      ds.actionCall(_actionForce);

      ds.parallelEnd();
      ds.parallelBegin();

      ds.actionCall(_actionName);
      ds.parallelEnd();

      if (_applyShutdown) {
        ds.patternCall(_shutdownPattern);
      }
    }
    ds.sequentialEnd();
    // }
    // ds.parallelEnd();

    measurement.setSetups(ds);
  }

  @Override
  public void run() {
    MultiSiteDoubleArray intermediateResult;

    logTrace("DC_Measurement", "run");
    super.run();
    measurement.execute();

    // postBody should be added and called here

    // protect results to be not overwritten
//    IDigInOutActionResults actionResults = measurement.digInOut(_pin).preserveActionResults();
    IDcVIResults actionResultsDcVI = measurement.dcVI(_pin).preserveResults();
    funcResult = measurement.preserveResult();

    // Assume for now that if force pass is set then branching decision could be dependent on the
    // result of this test, in future add another attribute to control async processing on/off
    if (!sync_par && !forcePass) {
      releaseTester();
    }

    // Make the DC measurement result available
    if (_measure == MEAS.VOLT) {
//      IIforceVmeasResults ifvmResults = actionResults.iforceVmeas(_actionName);
     IVmeasResults vmeasResults= actionResultsDcVI.vmeas(_actionName);
      intermediateResult = vmeasResults.getVoltage(_pin);

    } else { // MEAS.CURR
//      IVforceImeasResults ifvmResults = actionResults.vforceImeas(_actionName);
//      intermediateResult = ifvmResults.getCurrent(_pin);
      IImeasResults imeasResults= actionResultsDcVI.imeas(_actionName);
      intermediateResult = imeasResults.getCurrent(_pin);
    }

    // Loop through the sites to get the data
    _result = new MultiSiteDouble();
    for (int site : context.getActiveSites()) {
      if (intermediateResult.get(site).length > 1) {
        message(
            1,
            "Warning: not all measurements are logged, only the first one, change in DC_Measurement.java to include more");
      }
      _result.set(site, intermediateResult.get(site)[0]);
    }
  }

  @Override
  public void processResults() {
    judgeAndDatalog(FUNC, funcResult);

    judgeAndDatalog(PAR, filterResult(_result));
  }

  public MultiSiteDouble filterResult(MultiSiteDouble MSD) {
    return MSD;
  }
}


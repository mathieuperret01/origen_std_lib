#ifndef ORIGEN_TEST_METHOD_DC_MEASUREMENT_INCLUDED
#define ORIGEN_TEST_METHOD_DC_MEASUREMENT_INCLUDED

#include "base.hpp"
#include "mapi.hpp"
#include "rdi.hpp"

using namespace std;

namespace Origen {
namespace TestMethod {

class DCMeasurement: public Base {
    void serialProcessing(int site);

    int _applyShutdown;
    string _shutdownPattern;
    string _measure;
    double _settlingTime;
    string _pin;
    double _forceValue;
    int _iRange;

public:
    // Defaults
    DCMeasurement() {
        applyShutdown(1);
        measure("VOLT");
        settlingTime(0);
        forceValue(0);
        iRange(0);
    }

    virtual ~DCMeasurement() { }
    void SMC_backgroundProcessing();
    void execute();

    DCMeasurement & applyShutdown(int v) { _applyShutdown = v; return *this; }
    DCMeasurement & shutdownPattern(string v) { _shutdownPattern = v; return *this; }
    DCMeasurement & measure(string v) { _measure = v; return *this; }
    DCMeasurement & settlingTime(double v) { _settlingTime = v; return *this; }
    DCMeasurement & pin(string v) { _pin = v; return *this; }
    DCMeasurement & forceValue(double v) { _forceValue = v; return *this; }
    DCMeasurement & iRange(int v) { _iRange = v; return *this; }

protected:
    // All test methods must implement this function
    DCMeasurement & getThis() { return *this; }

    // Member/instance variables, declared outside the execute function body since
    // they may be useful to refer to in callback functions
    ARRAY_I activeSites;
    string testSuiteName;
    string label;
    vector<int> funcResults;
    vector<double> results;
    LIMIT limits;
};

void DCMeasurement::execute() {

    int site, physicalSites;
    ARRAY_I sites;

    RDI_INIT();

    ON_FIRST_INVOCATION_BEGIN();

        enableHiddenUpload();
        GET_ACTIVE_SITES(activeSites);
        physicalSites = GET_CONFIGURED_SITES(sites);
        results.resize(physicalSites + 1);
        funcResults.resize(physicalSites + 1);
        GET_TESTSUITE_NAME(testSuiteName);
        label = Primary.getLabel();

        pin(extractPinsFromGroup(_pin));

        if (_applyShutdown) {
            if (_shutdownPattern.empty()) {
                ostringstream pat;
                pat << label << "_part1";
                shutdownPattern(pat.str());
            }
        }

        //If forcing current, derive iRange from force value,
        //else we're forcing voltage, derive iRange from limits
        limits = GET_LIMIT_OBJECT("Functional");
        if (!_iRange) {
            if (_measure == "CURR") {
                _iRange = autorange(_forceValue);
            } else {
                //_iRange = autorange(limits);
                //cout << "ERROR: autorange is not supported yet for voltage measure, you must supply it" << endl;
                //ERROR_EXIT(TM::ABORT_FLOW);
            }
        }

        RDI_BEGIN();

        if (preTestFunc()) {
            rdi.func("f1").label(label).execute();

            if (holdStateFunc()) {
                if(_measure == "VOLT") {

                    rdi.dc(testSuiteName).pin(_pin)
                          .iForce(_forceValue)
                          .relay(TA::ppmuRly_onPPMU_offACDC,TA::ppmuRly_onAC_offDCPPMU)
                          .measWait(_settlingTime)
                          .vMeas()
                          .execute();
                } else {

                    rdi.dc(testSuiteName).pin(_pin)
                          .vForce(_forceValue)
                          .relay(TA::ppmuRly_onPPMU_offACDC,TA::ppmuRly_onAC_offDCPPMU)
                          .measWait(_settlingTime)
                          .iRange(_iRange)
                          .iMeas()
                          .execute();
                }
            }
            if (_applyShutdown) rdi.func("f2").label(_shutdownPattern).execute();
        }

        RDI_END();

        postTestFunc();

        FOR_EACH_SITE_BEGIN();
            site = CURRENT_SITE_NUMBER();
            funcResults[site] = rdi.id("f1").getPassFail() && rdi.id("f2").getPassFail();
            // TODO: This retrieval needs to move to the SMC func in the async case
            results[site] = rdi.id(testSuiteName).getValue();
            //            // Multiplier to make measured value units
            //            // match limits, default Imeas is amps (A)
            //            double mult = getUnitMultiplier(limit.Units);
            //
            //            if (_measure == "CURR")
            //                measResult = measResult * mult;
        FOR_EACH_SITE_END();

        asyncProcessing(this);

    ON_FIRST_INVOCATION_END();

    finalProcessing();

}

void DCMeasurement::serialProcessing(int site) {
    TESTSET().judgeAndLog_FunctionalTest(funcResults[site]);
    TESTSET().judgeAndLog_ParametricTest(_pin, testSuiteName, limits, filterResult(results[site]));
}

void DCMeasurement::SMC_backgroundProcessing() {
    if (processFunc()) {
        for (int i = 0; i < activeSites.size(); i++) {
            int site = activeSites[i];
            SMC_TEST(site, "", testSuiteName, LIMIT(TM::GE, 1, TM::LE, 1), funcResults[site]);
            SMC_TEST(site, _pin, testSuiteName, limits, filterResult(results[site]));
        }
    }
    postProcessFunc();
}

}
}
#endif

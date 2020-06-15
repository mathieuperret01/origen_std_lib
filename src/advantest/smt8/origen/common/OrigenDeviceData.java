package origen.common;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import origen.test_methods.Base;
import xoc.dta.UncheckedDTAException;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.MultiSiteLongArray;
import xoc.dta.datatypes.MultiSiteString;
import xoc.dta.resultaccess.datatypes.MultiSiteBitSequence;

public class OrigenDeviceData {


    /** Instances of this class. 1 per test suite that accesses global variables **/
    private static Map<Base, OrigenDeviceData> instances = new HashMap<Base, OrigenDeviceData>();

    /** The global storage container of all variables of all types **/
    private static Map<OrigenVAR, OrigenDeviceDataTypeBase> varsInUse = new HashMap<OrigenVAR, OrigenDeviceDataTypeBase>();


    public interface OrigenVAR{

        Class<? extends OrigenDeviceDataTypeBase> getType();

    }

    /** Local **/

    /** Base Reference of this DeviceData instance to testmethod instance **/
    private Base tmRef = null;


    /** List of Variables that have been reserved for the current execution of this DeviceData Instance. Always gets cleared at end of testsuite execution. */
    private List<OrigenVAR> reservedDeviceDataVars = new ArrayList<OrigenVAR>();


    /** Acquire OrigenDeviceData instance for this testsuite
     * <p> Usage<br>
     * From TM call as : OrigenDeviceData.getInstance(this)<br>
     * It is safe and without test time impact to always call getInstance() when OrigenDeviceData is needed.<br>
     * It is equally ok to call getInstance once and keep the reference either as local or as class variable<br>
     *
     * @param _tmRef the testmethod itself. pass in "this"
     * @return OrigenDeviceData
     */
    public static OrigenDeviceData getInstance(Base _tmRef)
    {
        OrigenDeviceData devData = instances.get(_tmRef);
        if (devData == null)
        {
            devData = new OrigenDeviceData(_tmRef);
            instances.put(_tmRef, devData);
        }
        _tmRef.setOrigenDeviceDataStorage(devData);
        return devData;
    }

    /**
     * Returns a String containing the content of the entire global DeviceDataStorage for printout
     * @return dump
     */
    public static String dumpVariables()
    {
        StringBuilder log = new StringBuilder();

        log.append("[DeviceData] ******* Dump of Variable Storage *********\n" +
        "[DeviceData] ****** Name *******  Value ******* Type\n");
        for (Map.Entry<OrigenVAR, OrigenDeviceDataTypeBase> entry : varsInUse.entrySet())
        {
            log.append("[OrigenDeviceData] " + entry.getKey() + ": \t" + entry.getValue() + " \t\t(" + entry.getKey().getType() + "\n");
        }
        return log.toString();
    }

    /** This clears the data storage, and should be called before each run, e.g. from preRun
     */
    public static void clearStorage()
    {
        varsInUse.clear();
    }

    /** Constructor. Use getInstance instead
     * @param _tmRef : SimplifiedBase
     */
    private OrigenDeviceData(Base _tmRef)
    {
        tmRef = _tmRef;
    }




    /** Base class for global variables */
    public static abstract class OrigenDeviceDataTypeBase
    {
        protected String name;
        protected Semaphore lock = new Semaphore(1);
        protected long timeOut_us = 10000000;

        //protected SimplifiedBase reservedBy = null; //might add this to track which suite reserved this, for better error reporting

        public OrigenDeviceDataTypeBase()
        {
        }
        protected OrigenDeviceDataTypeBase (String _name)
        {
            name = _name;
        }

        public void setName(String _name)
        {
            name = _name;
        }

        /** Acquires lock on a variable.
         * <p>This method returns immediately, unless some one else already keeps a lock on this variable,
         * e.g. the previous testsuite.
         * In this case it waits until lock is released or a timeout occurs
         * If a wait happens, this wait is part of the foreground of a testsuite, before release93k().<br>
         *
         * In the .prog Test program file,  the timing profile can be enabled to printout information
         * about any Device Data wait times during program execution.<br>
         * var timingProfileEnable = true;
         * @param _msg The message logger
         * @return true if lock acquired successfully
         */
        protected boolean lock()
        {
            boolean status = false;
            try
            {
                status = lock.tryAcquire(timeOut_us, TimeUnit.MICROSECONDS);

            }
            catch(InterruptedException e)
            {
                throw new UncheckedDTAException("Trying to acquire lock on OrigenDeviceDataVariable " + name + ", but background operation was interrupted", e);
            }
            return status;
        }

        /** Release the lock on a variable. used internally */
        protected void release()
        {
            if(0 == lock.availablePermits())
            {
                lock.release();
            }
        }

    }


    /** Global Variable Class to hold MultiSiteDouble values **/
    public static class OrigenDeviceDataDouble extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteDouble val;

        public OrigenDeviceDataDouble()
        {
            super();
        }

        public OrigenDeviceDataDouble (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteDouble _val)
        {
            val = _val;
        }

        public MultiSiteDouble get()
        {
            return val;
        }



        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            return val.toString();
        }

    }

    /** Global Variable Class to hold Map String, MultiSiteDouble values **/
    public static class OrigenDeviceDataMapDouble extends OrigenDeviceDataTypeBase
    {
        protected HashMap<String, MultiSiteDouble> val;

        public OrigenDeviceDataMapDouble()
        {
            super();
        }

        public OrigenDeviceDataMapDouble (String _name)
        {
            super(_name);
        }
        public void set(HashMap<String, MultiSiteDouble> _val)
        {
            val = _val;
        }

        public HashMap<String, MultiSiteDouble> get()
        {
            return val;
        }

        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            StringBuilder log = new StringBuilder();

            for (Entry<String, MultiSiteDouble> entry : val.entrySet())
            {
                log.append("[" + entry.getKey() + "] " + val.get(entry.getKey()).toString() + " ** ");
            }
            return log.toString();
        }

    }
    /** Global Variable Class to hold MultiSiteString values **/
    public static class OrigenDeviceDataString extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteString val;

        public OrigenDeviceDataString()
        {
            super();
        }

        public OrigenDeviceDataString (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteString _val)
        {
            val = _val;
        }

        public MultiSiteString get()
        {
            return val;
        }


        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            return val.toString();
        }
    }

    /** Global Variable Class to hold MultiSiteBoolean values **/
    public static class OrigenDeviceDataBoolean extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteBoolean val;

        public OrigenDeviceDataBoolean()
        {
            super();
        }

        public OrigenDeviceDataBoolean (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteBoolean _val)
        {
            val = _val;
        }

        public MultiSiteBoolean get()
        {
            return val;
        }

        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            return val.toString();
        }
    }

    /** Global Variable Class to hold MultiSiteLongArray values **/
    public static class OrigenDeviceDataLongArray extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteLongArray val;

        public OrigenDeviceDataLongArray()
        {
            super();
        }

        public OrigenDeviceDataLongArray (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteLongArray _val)
        {
            val = _val;
        }

        public MultiSiteLongArray get()
        {
            return val;
        }

        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            StringBuilder log = new StringBuilder();

            for (MultiSiteLong elem : val.getElements())
            {
                log.append(elem.toString() + " ** ");
            }
            return log.toString();
        }
    }


    /** Global Variable Class to hold MultiSiteLong values **/
    public static class OrigenDeviceDataLong extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteLong val;

        public OrigenDeviceDataLong()
        {
            super();
        }

        public OrigenDeviceDataLong (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteLong _val)
        {
            val = _val;
        }

        public MultiSiteLong get()
        {
            return val;
        }

        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            return val.toString();
        }
    }

    /** Global Variable Class to hold MultiSiteBitSequence values **/
    public static class OrigenDeviceDataBitSequence extends OrigenDeviceDataTypeBase
    {
        protected MultiSiteBitSequence val;

        public OrigenDeviceDataBitSequence()
        {
            super();
        }

        public OrigenDeviceDataBitSequence (String _name)
        {
            super(_name);
        }
        public void set(MultiSiteBitSequence _val)
        {
            val = _val;
        }

        public MultiSiteBitSequence get()
        {
            return val;
        }

        @Override public String toString()
        {
            if (val == null) {
                return "Value not set";
            }
            return val.toString();
        }
    }


/** Release lock on all variables held by this Testsuite/DeviceData instance **
 *  This will ideally be done in a base calls of all testmethod at the end of execute() instead of in each testmethod
 *  @see util.SimplifiedBase
 */
    public void releaseVariables()
    {
        for (OrigenVAR varName : reservedDeviceDataVars)
        {
            varsInUse.get(varName).release();
        }
        reservedDeviceDataVars.clear();
    }

    /** Release lock on a single variable held by this Testsuite/DeviceData instance
     * @param varName : DeviceData.VAR
     */
    public void releaseVarName(OrigenVAR varName)
    {
        OrigenDeviceDataTypeBase var = varsInUse.get(varName);
        if (var == null) //sanity check. should not happen
        {
            throw new UncheckedDTAException("Unexpected Error. Variable " + varName.toString() + " not yet created.");
        }
        var.release();

        reservedDeviceDataVars.remove(varName);
    }

    /** Acquire access to a variable. This can mean creating them, or just acquiring the lock from an existing variable for both read and write
     * @param name : DeviceData.VAR
     * @param allowCreate : boolean
     * @return DeviceDataTypeBase
     */
    private OrigenDeviceDataTypeBase getVariableAccess(OrigenVAR name, boolean allowCreate)
    {
        /**
         * if after release tester, but var is not in reserved list -> error
         * if after release tester and var is in reserved list can be sure var has been created during reserve, and locked. get it from storage and return
         *
         * if in foreground, we may get here from reserve or from set/get in a non background testsuite
         * - from reserve: get Var from storage:
         *      - if exists, acquire lock and return it
         *      - if not exists, create variable, lock it, add to varsInUse and return it
         * - from set: get Var from storage:
         *      - if exists, acquire lock and return it
         *      - if not exists, create variable, lock it, add to varsInUse, and return it
         *  => in both cases, if it is not yet in the reservedList, add it
         *  => no difference whether from reserve or from set
         *
         * - from get:
         *      - if exists, acquire lock. if not yet in reservedList, add it. Then return it
         *      - if not exists => error
         */

        // after Release93k
        if (tmRef.hasRelease93kBeenCalled())
        {
            if (reservedDeviceDataVars.contains(name) == false)
            {
                throw new UncheckedDTAException("Trying to access DeviceData variable after release93k() without reserving it. Var name: " + name.toString() +
                        "\n DeviceData.reserve(...) must be called for this variable before release93k()");
            }

            OrigenDeviceDataTypeBase var = varsInUse.get(name);
            if (var == null) //sanity check. should not happen
            {
                throw new UncheckedDTAException("Unexpected Error. Variable " + name.toString() + " not yet created.");
            }

            return var;
        }

        // Foreground. from reserve or get/set
        OrigenDeviceDataTypeBase var = varsInUse.get(name);
        if (var == null)
        {
            if (allowCreate == false) //this is called by a get() method.
            {
                throw new UncheckedDTAException("Trying to read a DeviceData variable before it has been created " + name.toString());
            }
            //there can not be race condition of multiple threads trying to create the same variable, because creating is only
            //possible in foreground
            try {
                //DeviceDataTypeBase newVar = name.getType().newInstance();
                Constructor<? extends OrigenDeviceDataTypeBase> constructor = name.getType().getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                var = constructor.newInstance(name.toString());

            }
            catch (RuntimeException e)
            {
                throw new UncheckedDTAException( "Error creating DeviceData variable " + name.toString() ,e);
            }
            catch (Exception e)
            {
                throw new UncheckedDTAException("Error creating DeviceData variable " + name.toString() ,e);
            }

            varsInUse.put(name, var);
        }

        //Put it into the reserved list. this allows for multiple accesses to this variable in the same foreground code
        //without locking up
        //Only the first access may and must acquire the lock

        //it is already in the list, we have the lock. return it.
        if (reservedDeviceDataVars.contains(name)) {
            return var;
        }

        //not reserved yet, wait for lock.
        if (false == var.lock())
        {
            throw new UncheckedDTAException("Trying to acquire lock on DeviceDataVariable " + name.toString() + " timed out.");
        }

        reservedDeviceDataVars.add(name);

        return var;
    }

    /**
     * Release a variable before end of testsuite
     * <p>If a variable is only used to get/set in the foreground, i.e. before
     * release tester, there is no need to keep a lock on it until the end of
     * the entire testsuite. The lock can be immediately released.<br>
     *
     * <p>Sometimes DeviceData needs to be accessed to do runtime updates to a following measurement for example.
     * This is a foreground access.
     * We had some cases where unnecessarily the lock was kept until the end of entire testsuite, resulting in
     * wait times during the following testsuite.
     *
     * <p>However, there exist hybrid cases, where a variable is used in both fore-
     * and background.<br>
     * reserve(varA)<br>
     * get(varA)<br>
     * release93k()<br>
     * set(varA)<br>
     *
     * <p>in this case the foreground get(varA) may not release the lock on this variable.
     * to catch this, the caller of this method needs to check first, if reserve() has been
     * called for this variable.
     *
     * @param name : DeviceData.VAR
     */
    private void releaseVariableInForeground(OrigenVAR name)
    {
        if (tmRef.hasRelease93kBeenCalled())
        {
            //we are in background. premature auto release not possible. More get/set operations in background might follow this one.
            return;
        }

        OrigenDeviceDataTypeBase var = varsInUse.get(name);
        if (var == null) //sanity check. should not happen
        {
            throw new UncheckedDTAException("Unexpected Error. Variable " + name.toString() + " not yet created.");
        }

        releaseVarName(name);
    }


/****** Public methods to be called from a testmethod ****/


    /** Marking Variables as reserved
     *
     * <p>Must be called before release93k()
     * <p> It is always ok and safe to call reserve.
     * It is mandatory to call reserve if the variable is to be accessed after release93k()
     * If the variable is only accessed in the foreground, can immediately call get/set without prior reserve.
     * While reserved, only the current testsuite can read and write to them
     * They are automatically released when the testsuite completes its execution including any background tasks.
     *
     * <p>Call as
     * <pre>{@code
     * devData.reserve(VAR.da_vref_hq_trim);
     * or
     * devData.reserve(VAR.da_vref_hq_trim, VAR.da_vref_lq_trim);
     * }</pre>
     *
     * @param _variableNames : DeviceData.VAR...
     */
    public void reserve(OrigenVAR... _variableNames)
    {
        if (tmRef.hasRelease93kBeenCalled())
        {
            throw new UncheckedDTAException("reserve() must be called before release93k() in testsuite " + tmRef.getContext().getTestSuiteName());
        }
        for (OrigenVAR varName : _variableNames)
        {
            if (reservedDeviceDataVars.contains(varName))
            {
                continue;
            }
            getVariableAccess(varName, true);
        }
    }
    /** Add new Value to a List Variable. If this is first time use, creates the variable
     * @param name : DeviceData.VAR name
     * @param value : HashMap String,MultiSiteDouble
     */
    public void set(OrigenVAR name, HashMap<String, MultiSiteDouble> value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataMapDouble) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataMapDouble.");
        }
        ((OrigenDeviceDataMapDouble)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }


    /** Set new Value to Variable. If this is first time use, creates the variable *
     * @param name : DeviceData.VAR
     * @param value : MultiSiteDouble
     */
    public void set(OrigenVAR name, MultiSiteDouble value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);

        if ((var instanceof OrigenDeviceDataDouble) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataDouble.");
        }
        ((OrigenDeviceDataDouble)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Set new Value to Variable. If this is first time use, creates the variable
     * @param name : DeviceData.VAR name
     * @param value : MultiSiteString
     */
    public void set(OrigenVAR name, MultiSiteString value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataString) == false)
        {
            throw new UncheckedDTAException( "DeviceData Variable " + name.toString() + " is not of type DeviceDataString.");
        }
        ((OrigenDeviceDataString)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Set new Value to Variable. If this is first time use, creates the variable
     * @param name : DeviceData.VAR
     * @param value : value
     */
    public void set(OrigenVAR name, MultiSiteBoolean value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataBoolean) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataBoolean.");
        }
        ((OrigenDeviceDataBoolean)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Set new Value to Variable. If this is first time use, creates the variable
     * @param name : DeviceData.VAR
     * @param value : MultiSiteLongArray
     */
    public void set(OrigenVAR name, MultiSiteLongArray value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataLongArray) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataLongArray.");
        }
        ((OrigenDeviceDataLongArray)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Set new Value to Variable. If this is first time use, creates the variable *
     * @param name : DeviceData.VAR
     * @param value : MultiSiteLong
     */
    public void set(OrigenVAR name, MultiSiteLong value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataLong) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataLong.");
        }
        ((OrigenDeviceDataLong)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Set new Value to Variable. If this is first time use, creates the variable
     * @param name : DeviceData.VAR name
     * @param value : MultiSiteBitSequence
     */
    public void set(OrigenVAR name, MultiSiteBitSequence value)
    {
        /** a set() is only valid if
         * - we are in the foreground and no other testsuite has a lock on this variable.
         *      a variation of this is that the variable has not been created yet, we create it here and lock it.
         * - we are in the background and the variable is in the reservedDeviceDataVars list for this test suite
         */

        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, true);
        if ((var instanceof OrigenDeviceDataBitSequence) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataBitSequence.");
        }
        ((OrigenDeviceDataBitSequence)var).set(value);

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }
    }

    /** Get MultiSiteDouble from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteDouble
     */
    public MultiSiteDouble getDouble(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }


        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataDouble) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataDouble.");
        }
        MultiSiteDouble ret = ((OrigenDeviceDataDouble)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;


    }

    /** Get MultiSiteString from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteString
     */
    public MultiSiteString getString(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataString) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataString.");
        }

        MultiSiteString ret = ((OrigenDeviceDataString)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

    /** Get MultiSiteBoolean from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteBoolean
     */
    public MultiSiteBoolean getBoolean(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataBoolean) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataBoolean.");
        }

        MultiSiteBoolean ret = ((OrigenDeviceDataBoolean)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

    /** Get Map of MultiSiteDouble from global storage
     * @param name : DeviceData.VAR
     * @return HashMap String,MultiSiteDouble
     */
    public HashMap<String, MultiSiteDouble> getMapOfDouble(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataMapDouble) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataMapDouble.");
        }

        HashMap<String, MultiSiteDouble> ret = ((OrigenDeviceDataMapDouble)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

    /** Get MultiSiteLongArray from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteLongArray
     */
    public MultiSiteLongArray getLongArray(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataLongArray) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataLongArray.");
        }

        MultiSiteLongArray ret = ((OrigenDeviceDataLongArray)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

    /** Get MultiSiteLong from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteLong
     */
    public MultiSiteLong getLong(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataLong) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataLong.");
        }

        MultiSiteLong ret = ((OrigenDeviceDataLong)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

    /** Get MultiSiteBitSequence from global storage
     * @param name : DeviceData.VAR
     * @return MultiSiteBitSequence
     */
    public MultiSiteBitSequence getBitSequence(OrigenVAR name)
    {
        /** Check if Var is Reserved already by a reserve call
         * If yes, skip attempt to release variable in foreground.
         */
        boolean isAlreadyReserved = false;
        if (reservedDeviceDataVars.contains(name)) { isAlreadyReserved = true; }

        OrigenDeviceDataTypeBase var = getVariableAccess(name, false);
        if ((var instanceof OrigenDeviceDataBitSequence) == false)
        {
            throw new UncheckedDTAException("DeviceData Variable " + name.toString() + " is not of type DeviceDataBitSequence.");
        }

        MultiSiteBitSequence ret = ((OrigenDeviceDataBitSequence)var).get();

        if (isAlreadyReserved == false) {
            releaseVariableInForeground(name);
        }

        return ret;
    }

}

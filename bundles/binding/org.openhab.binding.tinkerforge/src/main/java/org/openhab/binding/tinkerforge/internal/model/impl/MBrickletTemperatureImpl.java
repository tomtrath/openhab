/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/**
 */
package org.openhab.binding.tinkerforge.internal.model.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.openhab.binding.tinkerforge.internal.TinkerforgeErrorHandler;
import org.openhab.binding.tinkerforge.internal.model.MBrickd;
import org.openhab.binding.tinkerforge.internal.model.MBrickletTemperature;
import org.openhab.binding.tinkerforge.internal.model.MSensor;
import org.openhab.binding.tinkerforge.internal.model.MTFConfigConsumer;
import org.openhab.binding.tinkerforge.internal.model.ModelPackage;
import org.openhab.binding.tinkerforge.internal.model.TFBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>MBricklet Temperature</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getLogger <em>Logger</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getUid <em>Uid</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getEnabledA <em>Enabled A</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getTinkerforgeDevice <em>Tinkerforge Device</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getIpConnection <em>Ip Connection</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getConnectedUid <em>Connected Uid</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getPosition <em>Position</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getDeviceIdentifier <em>Device Identifier</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getBrickd <em>Brickd</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getSensorValue <em>Sensor Value</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getCallbackPeriod <em>Callback Period</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getTfConfig <em>Tf Config</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getDeviceType <em>Device Type</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getTemperature <em>Temperature</em>}</li>
 *   <li>{@link org.openhab.binding.tinkerforge.internal.model.impl.MBrickletTemperatureImpl#getThreshold <em>Threshold</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MBrickletTemperatureImpl extends MinimalEObjectImpl.Container implements MBrickletTemperature
{
  /**
   * The default value of the '{@link #getLogger() <em>Logger</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLogger()
   * @generated
   * @ordered
   */
  protected static final Logger LOGGER_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getLogger() <em>Logger</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLogger()
   * @generated
   * @ordered
   */
  protected Logger logger = LOGGER_EDEFAULT;

  /**
   * The default value of the '{@link #getUid() <em>Uid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getUid()
   * @generated
   * @ordered
   */
  protected static final String UID_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getUid() <em>Uid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getUid()
   * @generated
   * @ordered
   */
  protected String uid = UID_EDEFAULT;

  /**
   * The default value of the '{@link #getEnabledA() <em>Enabled A</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getEnabledA()
   * @generated
   * @ordered
   */
  protected static final AtomicBoolean ENABLED_A_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getEnabledA() <em>Enabled A</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getEnabledA()
   * @generated
   * @ordered
   */
  protected AtomicBoolean enabledA = ENABLED_A_EDEFAULT;

  /**
   * The cached value of the '{@link #getTinkerforgeDevice() <em>Tinkerforge Device</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTinkerforgeDevice()
   * @generated
   * @ordered
   */
  protected BrickletTemperature tinkerforgeDevice;

  /**
   * The default value of the '{@link #getIpConnection() <em>Ip Connection</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getIpConnection()
   * @generated
   * @ordered
   */
  protected static final IPConnection IP_CONNECTION_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getIpConnection() <em>Ip Connection</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getIpConnection()
   * @generated
   * @ordered
   */
  protected IPConnection ipConnection = IP_CONNECTION_EDEFAULT;

  /**
   * The default value of the '{@link #getConnectedUid() <em>Connected Uid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getConnectedUid()
   * @generated
   * @ordered
   */
  protected static final String CONNECTED_UID_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getConnectedUid() <em>Connected Uid</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getConnectedUid()
   * @generated
   * @ordered
   */
  protected String connectedUid = CONNECTED_UID_EDEFAULT;

  /**
   * The default value of the '{@link #getPosition() <em>Position</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPosition()
   * @generated
   * @ordered
   */
  protected static final char POSITION_EDEFAULT = '\u0000';

  /**
   * The cached value of the '{@link #getPosition() <em>Position</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPosition()
   * @generated
   * @ordered
   */
  protected char position = POSITION_EDEFAULT;

  /**
   * The default value of the '{@link #getDeviceIdentifier() <em>Device Identifier</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDeviceIdentifier()
   * @generated
   * @ordered
   */
  protected static final int DEVICE_IDENTIFIER_EDEFAULT = 0;

  /**
   * The cached value of the '{@link #getDeviceIdentifier() <em>Device Identifier</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDeviceIdentifier()
   * @generated
   * @ordered
   */
  protected int deviceIdentifier = DEVICE_IDENTIFIER_EDEFAULT;

  /**
   * The default value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected static final String NAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getName()
   * @generated
   * @ordered
   */
  protected String name = NAME_EDEFAULT;

  /**
   * The default value of the '{@link #getSensorValue() <em>Sensor Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSensorValue()
   * @generated
   * @ordered
   */
  protected static final double SENSOR_VALUE_EDEFAULT = 0.0;

  /**
   * The cached value of the '{@link #getSensorValue() <em>Sensor Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSensorValue()
   * @generated
   * @ordered
   */
  protected double sensorValue = SENSOR_VALUE_EDEFAULT;

  /**
   * The default value of the '{@link #getCallbackPeriod() <em>Callback Period</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getCallbackPeriod()
   * @generated
   * @ordered
   */
  protected static final long CALLBACK_PERIOD_EDEFAULT = 1000L;

  /**
   * The cached value of the '{@link #getCallbackPeriod() <em>Callback Period</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getCallbackPeriod()
   * @generated
   * @ordered
   */
  protected long callbackPeriod = CALLBACK_PERIOD_EDEFAULT;

  /**
   * The cached value of the '{@link #getTfConfig() <em>Tf Config</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTfConfig()
   * @generated
   * @ordered
   */
  protected TFBaseConfiguration tfConfig;

  /**
   * The default value of the '{@link #getDeviceType() <em>Device Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDeviceType()
   * @generated
   * @ordered
   */
  protected static final String DEVICE_TYPE_EDEFAULT = "bricklet_temperature";

  /**
   * The cached value of the '{@link #getDeviceType() <em>Device Type</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getDeviceType()
   * @generated
   * @ordered
   */
  protected String deviceType = DEVICE_TYPE_EDEFAULT;

  /**
   * The default value of the '{@link #getTemperature() <em>Temperature</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTemperature()
   * @generated
   * @ordered
   */
  protected static final short TEMPERATURE_EDEFAULT = 0;

  /**
   * The cached value of the '{@link #getTemperature() <em>Temperature</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTemperature()
   * @generated
   * @ordered
   */
  protected short temperature = TEMPERATURE_EDEFAULT;

  /**
   * The default value of the '{@link #getThreshold() <em>Threshold</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getThreshold()
   * @generated
   * @ordered
   */
  protected static final int THRESHOLD_EDEFAULT = 10;

  /**
   * The cached value of the '{@link #getThreshold() <em>Threshold</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getThreshold()
   * @generated
   * @ordered
   */
  protected int threshold = THRESHOLD_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected MBrickletTemperatureImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return ModelPackage.Literals.MBRICKLET_TEMPERATURE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Logger getLogger()
  {
    return logger;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLogger(Logger newLogger)
  {
    Logger oldLogger = logger;
    logger = newLogger;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__LOGGER, oldLogger, logger));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getUid()
  {
    return uid;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setUid(String newUid)
  {
    String oldUid = uid;
    uid = newUid;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__UID, oldUid, uid));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AtomicBoolean getEnabledA()
  {
    return enabledA;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setEnabledA(AtomicBoolean newEnabledA)
  {
    AtomicBoolean oldEnabledA = enabledA;
    enabledA = newEnabledA;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__ENABLED_A, oldEnabledA, enabledA));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletTemperature getTinkerforgeDevice()
  {
    return tinkerforgeDevice;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTinkerforgeDevice(BrickletTemperature newTinkerforgeDevice)
  {
    BrickletTemperature oldTinkerforgeDevice = tinkerforgeDevice;
    tinkerforgeDevice = newTinkerforgeDevice;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__TINKERFORGE_DEVICE, oldTinkerforgeDevice, tinkerforgeDevice));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IPConnection getIpConnection()
  {
    return ipConnection;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setIpConnection(IPConnection newIpConnection)
  {
    IPConnection oldIpConnection = ipConnection;
    ipConnection = newIpConnection;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__IP_CONNECTION, oldIpConnection, ipConnection));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getConnectedUid()
  {
    return connectedUid;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setConnectedUid(String newConnectedUid)
  {
    String oldConnectedUid = connectedUid;
    connectedUid = newConnectedUid;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__CONNECTED_UID, oldConnectedUid, connectedUid));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public char getPosition()
  {
    return position;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPosition(char newPosition)
  {
    char oldPosition = position;
    position = newPosition;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__POSITION, oldPosition, position));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public int getDeviceIdentifier()
  {
    return deviceIdentifier;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setDeviceIdentifier(int newDeviceIdentifier)
  {
    int oldDeviceIdentifier = deviceIdentifier;
    deviceIdentifier = newDeviceIdentifier;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_IDENTIFIER, oldDeviceIdentifier, deviceIdentifier));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getName()
  {
    return name;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setName(String newName)
  {
    String oldName = name;
    name = newName;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__NAME, oldName, name));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickd getBrickd()
  {
    if (eContainerFeatureID() != ModelPackage.MBRICKLET_TEMPERATURE__BRICKD) return null;
    return (MBrickd)eContainer();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetBrickd(MBrickd newBrickd, NotificationChain msgs)
  {
    msgs = eBasicSetContainer((InternalEObject)newBrickd, ModelPackage.MBRICKLET_TEMPERATURE__BRICKD, msgs);
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setBrickd(MBrickd newBrickd)
  {
    if (newBrickd != eInternalContainer() || (eContainerFeatureID() != ModelPackage.MBRICKLET_TEMPERATURE__BRICKD && newBrickd != null))
    {
      if (EcoreUtil.isAncestor(this, newBrickd))
        throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
      NotificationChain msgs = null;
      if (eInternalContainer() != null)
        msgs = eBasicRemoveFromContainer(msgs);
      if (newBrickd != null)
        msgs = ((InternalEObject)newBrickd).eInverseAdd(this, ModelPackage.MBRICKD__MDEVICES, MBrickd.class, msgs);
      msgs = basicSetBrickd(newBrickd, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__BRICKD, newBrickd, newBrickd));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public double getSensorValue()
  {
    return sensorValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSensorValue(double newSensorValue)
  {
    double oldSensorValue = sensorValue;
    sensorValue = newSensorValue;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE, oldSensorValue, sensorValue));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public long getCallbackPeriod()
  {
    return callbackPeriod;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setCallbackPeriod(long newCallbackPeriod)
  {
    long oldCallbackPeriod = callbackPeriod;
    callbackPeriod = newCallbackPeriod;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD, oldCallbackPeriod, callbackPeriod));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TFBaseConfiguration getTfConfig()
  {
    return tfConfig;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetTfConfig(TFBaseConfiguration newTfConfig, NotificationChain msgs)
  {
    TFBaseConfiguration oldTfConfig = tfConfig;
    tfConfig = newTfConfig;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG, oldTfConfig, newTfConfig);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTfConfig(TFBaseConfiguration newTfConfig)
  {
    if (newTfConfig != tfConfig)
    {
      NotificationChain msgs = null;
      if (tfConfig != null)
        msgs = ((InternalEObject)tfConfig).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG, null, msgs);
      if (newTfConfig != null)
        msgs = ((InternalEObject)newTfConfig).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG, null, msgs);
      msgs = basicSetTfConfig(newTfConfig, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG, newTfConfig, newTfConfig));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getDeviceType()
  {
    return deviceType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public short getTemperature()
  {
    return temperature;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTemperature(short newTemperature)
  {
    short oldTemperature = temperature;
    temperature = newTemperature;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__TEMPERATURE, oldTemperature, temperature));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public int getThreshold()
  {
    return threshold;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setThreshold(int newThreshold)
  {
    int oldThreshold = threshold;
    threshold = newThreshold;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.MBRICKLET_TEMPERATURE__THRESHOLD, oldThreshold, threshold));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  public void init()
  {
	setEnabledA(new AtomicBoolean());
	logger = LoggerFactory.getLogger(MBrickletTemperatureImpl.class);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */  
	@Override
	public void enable() {
	    tinkerforgeDevice = new BrickletTemperature(uid, getIpConnection());
		if (tfConfig != null) {
			if (tfConfig.eIsSet(tfConfig.eClass().getEStructuralFeature(
					"threshold"))) {
				setThreshold(tfConfig.getThreshold());
			}
			if (tfConfig.eIsSet(tfConfig.eClass().getEStructuralFeature(
					"callbackPeriod"))) {
				setCallbackPeriod(tfConfig.getCallbackPeriod());
			}
		}
	    try {
	    	tinkerforgeDevice.setResponseExpected(BrickletTemperature.FUNCTION_SET_TEMPERATURE_CALLBACK_PERIOD, false);
	    	tinkerforgeDevice.setTemperatureCallbackPeriod(callbackPeriod);
		} catch (TimeoutException e) {
			TinkerforgeErrorHandler.handleError(this,
					TinkerforgeErrorHandler.TF_TIMEOUT_EXCEPTION, e);
		} catch (NotConnectedException e) {
			TinkerforgeErrorHandler.handleError(this,
					TinkerforgeErrorHandler.TF_NOT_CONNECTION_EXCEPTION, e);
		}   	
	    tinkerforgeDevice.addTemperatureListener(new BrickletTemperature.TemperatureListener() {
			
			@Override
			public void temperature(short newTemperature) {
				if ((newTemperature > (temperature + threshold))  || (newTemperature < (temperature - threshold)))
				{
					setSensorValue(newTemperature/100.0);
					setTemperature(newTemperature);
				}
				else {
					logger.trace(String.format("new temperature: %s, old %s", newTemperature, temperature));
				}

			}
		});
	}

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
	@Override
	public void disable() {
		tinkerforgeDevice = null;
	}

/**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        if (eInternalContainer() != null)
          msgs = eBasicRemoveFromContainer(msgs);
        return basicSetBrickd((MBrickd)otherEnd, msgs);
    }
    return super.eInverseAdd(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        return basicSetBrickd(null, msgs);
      case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG:
        return basicSetTfConfig(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs)
  {
    switch (eContainerFeatureID())
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        return eInternalContainer().eInverseRemove(this, ModelPackage.MBRICKD__MDEVICES, MBrickd.class, msgs);
    }
    return super.eBasicRemoveFromContainerFeature(msgs);
  }

/**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
	@Override
	public Double fetchSensorValue() {
		try {
			//TODO do not return anything update model instead: thread safe?
			return (double) tinkerforgeDevice.getTemperature() / 100.0;
		} catch (TimeoutException e) {
			TinkerforgeErrorHandler.handleError(this,
					TinkerforgeErrorHandler.TF_TIMEOUT_EXCEPTION, e);
		} catch (NotConnectedException e) {
			TinkerforgeErrorHandler.handleError(this,
					TinkerforgeErrorHandler.TF_NOT_CONNECTION_EXCEPTION, e);
		}   	
		return null;
	}

/**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__LOGGER:
        return getLogger();
      case ModelPackage.MBRICKLET_TEMPERATURE__UID:
        return getUid();
      case ModelPackage.MBRICKLET_TEMPERATURE__ENABLED_A:
        return getEnabledA();
      case ModelPackage.MBRICKLET_TEMPERATURE__TINKERFORGE_DEVICE:
        return getTinkerforgeDevice();
      case ModelPackage.MBRICKLET_TEMPERATURE__IP_CONNECTION:
        return getIpConnection();
      case ModelPackage.MBRICKLET_TEMPERATURE__CONNECTED_UID:
        return getConnectedUid();
      case ModelPackage.MBRICKLET_TEMPERATURE__POSITION:
        return getPosition();
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_IDENTIFIER:
        return getDeviceIdentifier();
      case ModelPackage.MBRICKLET_TEMPERATURE__NAME:
        return getName();
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        return getBrickd();
      case ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE:
        return getSensorValue();
      case ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD:
        return getCallbackPeriod();
      case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG:
        return getTfConfig();
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_TYPE:
        return getDeviceType();
      case ModelPackage.MBRICKLET_TEMPERATURE__TEMPERATURE:
        return getTemperature();
      case ModelPackage.MBRICKLET_TEMPERATURE__THRESHOLD:
        return getThreshold();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__LOGGER:
        setLogger((Logger)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__UID:
        setUid((String)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__ENABLED_A:
        setEnabledA((AtomicBoolean)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TINKERFORGE_DEVICE:
        setTinkerforgeDevice((BrickletTemperature)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__IP_CONNECTION:
        setIpConnection((IPConnection)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__CONNECTED_UID:
        setConnectedUid((String)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__POSITION:
        setPosition((Character)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_IDENTIFIER:
        setDeviceIdentifier((Integer)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__NAME:
        setName((String)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        setBrickd((MBrickd)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE:
        setSensorValue((Double)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD:
        setCallbackPeriod((Long)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG:
        setTfConfig((TFBaseConfiguration)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TEMPERATURE:
        setTemperature((Short)newValue);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__THRESHOLD:
        setThreshold((Integer)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__LOGGER:
        setLogger(LOGGER_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__UID:
        setUid(UID_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__ENABLED_A:
        setEnabledA(ENABLED_A_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TINKERFORGE_DEVICE:
        setTinkerforgeDevice((BrickletTemperature)null);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__IP_CONNECTION:
        setIpConnection(IP_CONNECTION_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__CONNECTED_UID:
        setConnectedUid(CONNECTED_UID_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__POSITION:
        setPosition(POSITION_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_IDENTIFIER:
        setDeviceIdentifier(DEVICE_IDENTIFIER_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__NAME:
        setName(NAME_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        setBrickd((MBrickd)null);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE:
        setSensorValue(SENSOR_VALUE_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD:
        setCallbackPeriod(CALLBACK_PERIOD_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG:
        setTfConfig((TFBaseConfiguration)null);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__TEMPERATURE:
        setTemperature(TEMPERATURE_EDEFAULT);
        return;
      case ModelPackage.MBRICKLET_TEMPERATURE__THRESHOLD:
        setThreshold(THRESHOLD_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE__LOGGER:
        return LOGGER_EDEFAULT == null ? logger != null : !LOGGER_EDEFAULT.equals(logger);
      case ModelPackage.MBRICKLET_TEMPERATURE__UID:
        return UID_EDEFAULT == null ? uid != null : !UID_EDEFAULT.equals(uid);
      case ModelPackage.MBRICKLET_TEMPERATURE__ENABLED_A:
        return ENABLED_A_EDEFAULT == null ? enabledA != null : !ENABLED_A_EDEFAULT.equals(enabledA);
      case ModelPackage.MBRICKLET_TEMPERATURE__TINKERFORGE_DEVICE:
        return tinkerforgeDevice != null;
      case ModelPackage.MBRICKLET_TEMPERATURE__IP_CONNECTION:
        return IP_CONNECTION_EDEFAULT == null ? ipConnection != null : !IP_CONNECTION_EDEFAULT.equals(ipConnection);
      case ModelPackage.MBRICKLET_TEMPERATURE__CONNECTED_UID:
        return CONNECTED_UID_EDEFAULT == null ? connectedUid != null : !CONNECTED_UID_EDEFAULT.equals(connectedUid);
      case ModelPackage.MBRICKLET_TEMPERATURE__POSITION:
        return position != POSITION_EDEFAULT;
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_IDENTIFIER:
        return deviceIdentifier != DEVICE_IDENTIFIER_EDEFAULT;
      case ModelPackage.MBRICKLET_TEMPERATURE__NAME:
        return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
      case ModelPackage.MBRICKLET_TEMPERATURE__BRICKD:
        return getBrickd() != null;
      case ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE:
        return sensorValue != SENSOR_VALUE_EDEFAULT;
      case ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD:
        return callbackPeriod != CALLBACK_PERIOD_EDEFAULT;
      case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG:
        return tfConfig != null;
      case ModelPackage.MBRICKLET_TEMPERATURE__DEVICE_TYPE:
        return DEVICE_TYPE_EDEFAULT == null ? deviceType != null : !DEVICE_TYPE_EDEFAULT.equals(deviceType);
      case ModelPackage.MBRICKLET_TEMPERATURE__TEMPERATURE:
        return temperature != TEMPERATURE_EDEFAULT;
      case ModelPackage.MBRICKLET_TEMPERATURE__THRESHOLD:
        return threshold != THRESHOLD_EDEFAULT;
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass)
  {
    if (baseClass == MSensor.class)
    {
      switch (derivedFeatureID)
      {
        case ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE: return ModelPackage.MSENSOR__SENSOR_VALUE;
        case ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD: return ModelPackage.MSENSOR__CALLBACK_PERIOD;
        default: return -1;
      }
    }
    if (baseClass == MTFConfigConsumer.class)
    {
      switch (derivedFeatureID)
      {
        case ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG: return ModelPackage.MTF_CONFIG_CONSUMER__TF_CONFIG;
        default: return -1;
      }
    }
    return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass)
  {
    if (baseClass == MSensor.class)
    {
      switch (baseFeatureID)
      {
        case ModelPackage.MSENSOR__SENSOR_VALUE: return ModelPackage.MBRICKLET_TEMPERATURE__SENSOR_VALUE;
        case ModelPackage.MSENSOR__CALLBACK_PERIOD: return ModelPackage.MBRICKLET_TEMPERATURE__CALLBACK_PERIOD;
        default: return -1;
      }
    }
    if (baseClass == MTFConfigConsumer.class)
    {
      switch (baseFeatureID)
      {
        case ModelPackage.MTF_CONFIG_CONSUMER__TF_CONFIG: return ModelPackage.MBRICKLET_TEMPERATURE__TF_CONFIG;
        default: return -1;
      }
    }
    return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int eDerivedOperationID(int baseOperationID, Class<?> baseClass)
  {
    if (baseClass == MSensor.class)
    {
      switch (baseOperationID)
      {
        case ModelPackage.MSENSOR___FETCH_SENSOR_VALUE: return ModelPackage.MBRICKLET_TEMPERATURE___FETCH_SENSOR_VALUE;
        default: return -1;
      }
    }
    if (baseClass == MTFConfigConsumer.class)
    {
      switch (baseOperationID)
      {
        default: return -1;
      }
    }
    return super.eDerivedOperationID(baseOperationID, baseClass);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException
  {
    switch (operationID)
    {
      case ModelPackage.MBRICKLET_TEMPERATURE___INIT:
        init();
        return null;
      case ModelPackage.MBRICKLET_TEMPERATURE___FETCH_SENSOR_VALUE:
        return fetchSensorValue();
      case ModelPackage.MBRICKLET_TEMPERATURE___ENABLE:
        enable();
        return null;
      case ModelPackage.MBRICKLET_TEMPERATURE___DISABLE:
        disable();
        return null;
    }
    return super.eInvoke(operationID, arguments);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (logger: ");
    result.append(logger);
    result.append(", uid: ");
    result.append(uid);
    result.append(", enabledA: ");
    result.append(enabledA);
    result.append(", tinkerforgeDevice: ");
    result.append(tinkerforgeDevice);
    result.append(", ipConnection: ");
    result.append(ipConnection);
    result.append(", connectedUid: ");
    result.append(connectedUid);
    result.append(", position: ");
    result.append(position);
    result.append(", deviceIdentifier: ");
    result.append(deviceIdentifier);
    result.append(", name: ");
    result.append(name);
    result.append(", sensorValue: ");
    result.append(sensorValue);
    result.append(", callbackPeriod: ");
    result.append(callbackPeriod);
    result.append(", deviceType: ");
    result.append(deviceType);
    result.append(", temperature: ");
    result.append(temperature);
    result.append(", threshold: ");
    result.append(threshold);
    result.append(')');
    return result.toString();
  }

} //MBrickletTemperatureImpl

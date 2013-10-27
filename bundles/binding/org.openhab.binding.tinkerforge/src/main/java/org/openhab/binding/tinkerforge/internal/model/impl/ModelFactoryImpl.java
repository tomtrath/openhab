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

import com.tinkerforge.BrickDC;
import com.tinkerforge.BrickServo;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletBarometer;
import com.tinkerforge.BrickletDistanceIR;
import com.tinkerforge.BrickletDualRelay;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletIndustrialQuadRelay;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletTemperature;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.openhab.binding.tinkerforge.internal.model.*;
import org.openhab.binding.tinkerforge.internal.model.Ecosystem;
import org.openhab.binding.tinkerforge.internal.model.MBrickd;
import org.openhab.binding.tinkerforge.internal.model.MBrickletDistanceIR;
import org.openhab.binding.tinkerforge.internal.model.MBrickletHumidity;
import org.openhab.binding.tinkerforge.internal.model.MBrickletTemperature;
import org.openhab.binding.tinkerforge.internal.model.ModelFactory;
import org.openhab.binding.tinkerforge.internal.model.ModelPackage;
import org.slf4j.Logger;

import com.tinkerforge.Device;
import com.tinkerforge.IPConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelFactoryImpl extends EFactoryImpl implements ModelFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static ModelFactory init()
  {
    try
    {
      ModelFactory theModelFactory = (ModelFactory)EPackage.Registry.INSTANCE.getEFactory("org.openhab.binding.tinkerforge.internal.model"); 
      if (theModelFactory != null)
      {
        return theModelFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new ModelFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ModelFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case ModelPackage.OHTF_DEVICE: return createOHTFDevice();
      case ModelPackage.OH_CONFIG: return createOHConfig();
      case ModelPackage.ECOSYSTEM: return createEcosystem();
      case ModelPackage.MBRICKD: return createMBrickd();
      case ModelPackage.MBRICK_SERVO: return createMBrickServo();
      case ModelPackage.TF_BRICK_DC_CONFIGURATION: return createTFBrickDCConfiguration();
      case ModelPackage.MBRICK_DC: return createMBrickDC();
      case ModelPackage.MDUAL_RELAY_BRICKLET: return createMDualRelayBricklet();
      case ModelPackage.MINDUSTRIAL_QUAD_RELAY_BRICKLET: return createMIndustrialQuadRelayBricklet();
      case ModelPackage.MINDUSTRIAL_QUAD_RELAY: return createMIndustrialQuadRelay();
      case ModelPackage.MDUAL_RELAY: return createMDualRelay();
      case ModelPackage.TF_NULL_CONFIGURATION: return createTFNullConfiguration();
      case ModelPackage.TF_SERVO_CONFIGURATION: return createTFServoConfiguration();
      case ModelPackage.MSERVO: return createMServo();
      case ModelPackage.MBRICKLET_HUMIDITY: return createMBrickletHumidity();
      case ModelPackage.MBRICKLET_DISTANCE_IR: return createMBrickletDistanceIR();
      case ModelPackage.MBRICKLET_TEMPERATURE: return createMBrickletTemperature();
      case ModelPackage.TF_BASE_CONFIGURATION: return createTFBaseConfiguration();
      case ModelPackage.MBRICKLET_BAROMETER: return createMBrickletBarometer();
      case ModelPackage.MBAROMETER_TEMPERATURE: return createMBarometerTemperature();
      case ModelPackage.MBRICKLET_AMBIENT_LIGHT: return createMBrickletAmbientLight();
      case ModelPackage.MBRICKLET_LCD2_0X4: return createMBrickletLCD20x4();
      case ModelPackage.MLCD2_0X4_BUTTON: return createMLCD20x4Button();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object createFromString(EDataType eDataType, String initialValue)
  {
    switch (eDataType.getClassifierID())
    {
      case ModelPackage.SWITCH_STATE:
        return createSwitchStateFromString(eDataType, initialValue);
      case ModelPackage.DC_DRIVE_MODE:
        return createDCDriveModeFromString(eDataType, initialValue);
      case ModelPackage.MIP_CONNECTION:
        return createMIPConnectionFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_DEVICE:
        return createMTinkerDeviceFromString(eDataType, initialValue);
      case ModelPackage.MLOGGER:
        return createMLoggerFromString(eDataType, initialValue);
      case ModelPackage.MATOMIC_BOOLEAN:
        return createMAtomicBooleanFromString(eDataType, initialValue);
      case ModelPackage.MTINKERFORGE_DEVICE:
        return createMTinkerforgeDeviceFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICK_DC:
        return createMTinkerBrickDCFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_DUAL_RELAY:
        return createMTinkerBrickletDualRelayFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_INDUSTRIAL_QUAD_RELAY:
        return createMTinkerBrickletIndustrialQuadRelayFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICK_SERVO:
        return createMTinkerBrickServoFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_HUMIDITY:
        return createMTinkerBrickletHumidityFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_DISTANCE_IR:
        return createMTinkerBrickletDistanceIRFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_TEMPERATURE:
        return createMTinkerBrickletTemperatureFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_BAROMETER:
        return createMTinkerBrickletBarometerFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_AMBIENT_LIGHT:
        return createMTinkerBrickletAmbientLightFromString(eDataType, initialValue);
      case ModelPackage.MTINKER_BRICKLET_LCD2_0X4:
        return createMTinkerBrickletLCD20x4FromString(eDataType, initialValue);
      default:
        throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String convertToString(EDataType eDataType, Object instanceValue)
  {
    switch (eDataType.getClassifierID())
    {
      case ModelPackage.SWITCH_STATE:
        return convertSwitchStateToString(eDataType, instanceValue);
      case ModelPackage.DC_DRIVE_MODE:
        return convertDCDriveModeToString(eDataType, instanceValue);
      case ModelPackage.MIP_CONNECTION:
        return convertMIPConnectionToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_DEVICE:
        return convertMTinkerDeviceToString(eDataType, instanceValue);
      case ModelPackage.MLOGGER:
        return convertMLoggerToString(eDataType, instanceValue);
      case ModelPackage.MATOMIC_BOOLEAN:
        return convertMAtomicBooleanToString(eDataType, instanceValue);
      case ModelPackage.MTINKERFORGE_DEVICE:
        return convertMTinkerforgeDeviceToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICK_DC:
        return convertMTinkerBrickDCToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_DUAL_RELAY:
        return convertMTinkerBrickletDualRelayToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_INDUSTRIAL_QUAD_RELAY:
        return convertMTinkerBrickletIndustrialQuadRelayToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICK_SERVO:
        return convertMTinkerBrickServoToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_HUMIDITY:
        return convertMTinkerBrickletHumidityToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_DISTANCE_IR:
        return convertMTinkerBrickletDistanceIRToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_TEMPERATURE:
        return convertMTinkerBrickletTemperatureToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_BAROMETER:
        return convertMTinkerBrickletBarometerToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_AMBIENT_LIGHT:
        return convertMTinkerBrickletAmbientLightToString(eDataType, instanceValue);
      case ModelPackage.MTINKER_BRICKLET_LCD2_0X4:
        return convertMTinkerBrickletLCD20x4ToString(eDataType, instanceValue);
      default:
        throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public <TFC extends TFConfig> OHTFDevice<TFC> createOHTFDevice()
  {
    OHTFDeviceImpl<TFC> ohtfDevice = new OHTFDeviceImpl<TFC>();
    return ohtfDevice;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OHConfig createOHConfig()
  {
    OHConfigImpl ohConfig = new OHConfigImpl();
    return ohConfig;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Ecosystem createEcosystem()
  {
    EcosystemImpl ecosystem = new EcosystemImpl();
    return ecosystem;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickd createMBrickd()
  {
    MBrickdImpl mBrickd = new MBrickdImpl();
    return mBrickd;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickServo createMBrickServo()
  {
    MBrickServoImpl mBrickServo = new MBrickServoImpl();
    return mBrickServo;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TFBrickDCConfiguration createTFBrickDCConfiguration()
  {
    TFBrickDCConfigurationImpl tfBrickDCConfiguration = new TFBrickDCConfigurationImpl();
    return tfBrickDCConfiguration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickDC createMBrickDC()
  {
    MBrickDCImpl mBrickDC = new MBrickDCImpl();
    return mBrickDC;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDualRelayBricklet createMDualRelayBricklet()
  {
    MDualRelayBrickletImpl mDualRelayBricklet = new MDualRelayBrickletImpl();
    return mDualRelayBricklet;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MIndustrialQuadRelayBricklet createMIndustrialQuadRelayBricklet()
  {
    MIndustrialQuadRelayBrickletImpl mIndustrialQuadRelayBricklet = new MIndustrialQuadRelayBrickletImpl();
    return mIndustrialQuadRelayBricklet;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MIndustrialQuadRelay createMIndustrialQuadRelay()
  {
    MIndustrialQuadRelayImpl mIndustrialQuadRelay = new MIndustrialQuadRelayImpl();
    return mIndustrialQuadRelay;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MDualRelay createMDualRelay()
  {
    MDualRelayImpl mDualRelay = new MDualRelayImpl();
    return mDualRelay;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TFNullConfiguration createTFNullConfiguration()
  {
    TFNullConfigurationImpl tfNullConfiguration = new TFNullConfigurationImpl();
    return tfNullConfiguration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TFServoConfiguration createTFServoConfiguration()
  {
    TFServoConfigurationImpl tfServoConfiguration = new TFServoConfigurationImpl();
    return tfServoConfiguration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MServo createMServo()
  {
    MServoImpl mServo = new MServoImpl();
    return mServo;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletHumidity createMBrickletHumidity()
  {
    MBrickletHumidityImpl mBrickletHumidity = new MBrickletHumidityImpl();
    return mBrickletHumidity;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletDistanceIR createMBrickletDistanceIR()
  {
    MBrickletDistanceIRImpl mBrickletDistanceIR = new MBrickletDistanceIRImpl();
    return mBrickletDistanceIR;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletTemperature createMBrickletTemperature()
  {
    MBrickletTemperatureImpl mBrickletTemperature = new MBrickletTemperatureImpl();
    return mBrickletTemperature;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TFBaseConfiguration createTFBaseConfiguration()
  {
    TFBaseConfigurationImpl tfBaseConfiguration = new TFBaseConfigurationImpl();
    return tfBaseConfiguration;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletBarometer createMBrickletBarometer()
  {
    MBrickletBarometerImpl mBrickletBarometer = new MBrickletBarometerImpl();
    return mBrickletBarometer;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBarometerTemperature createMBarometerTemperature()
  {
    MBarometerTemperatureImpl mBarometerTemperature = new MBarometerTemperatureImpl();
    return mBarometerTemperature;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletAmbientLight createMBrickletAmbientLight()
  {
    MBrickletAmbientLightImpl mBrickletAmbientLight = new MBrickletAmbientLightImpl();
    return mBrickletAmbientLight;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MBrickletLCD20x4 createMBrickletLCD20x4()
  {
    MBrickletLCD20x4Impl mBrickletLCD20x4 = new MBrickletLCD20x4Impl();
    return mBrickletLCD20x4;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public MLCD20x4Button createMLCD20x4Button()
  {
    MLCD20x4ButtonImpl mlcd20x4Button = new MLCD20x4ButtonImpl();
    return mlcd20x4Button;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SwitchState createSwitchStateFromString(EDataType eDataType, String initialValue)
  {
    SwitchState result = SwitchState.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertSwitchStateToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DCDriveMode createDCDriveModeFromString(EDataType eDataType, String initialValue)
  {
    DCDriveMode result = DCDriveMode.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertDCDriveModeToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IPConnection createMIPConnectionFromString(EDataType eDataType, String initialValue)
  {
    return (IPConnection)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMIPConnectionToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Device createMTinkerDeviceFromString(EDataType eDataType, String initialValue)
  {
    return (Device)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerDeviceToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Logger createMLoggerFromString(EDataType eDataType, String initialValue)
  {
    return (Logger)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMLoggerToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AtomicBoolean createMAtomicBooleanFromString(EDataType eDataType, String initialValue)
  {
    return (AtomicBoolean)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMAtomicBooleanToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Device createMTinkerforgeDeviceFromString(EDataType eDataType, String initialValue)
  {
    return (Device)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerforgeDeviceToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickDC createMTinkerBrickDCFromString(EDataType eDataType, String initialValue)
  {
    return (BrickDC)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickDCToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickServo createMTinkerBrickServoFromString(EDataType eDataType, String initialValue)
  {
    return (BrickServo)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickServoToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletHumidity createMTinkerBrickletHumidityFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletHumidity)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletHumidityToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletDistanceIR createMTinkerBrickletDistanceIRFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletDistanceIR)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletDistanceIRToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletTemperature createMTinkerBrickletTemperatureFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletTemperature)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletTemperatureToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletBarometer createMTinkerBrickletBarometerFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletBarometer)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletBarometerToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletAmbientLight createMTinkerBrickletAmbientLightFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletAmbientLight)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletAmbientLightToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletLCD20x4 createMTinkerBrickletLCD20x4FromString(EDataType eDataType, String initialValue)
  {
    return (BrickletLCD20x4)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletLCD20x4ToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletDualRelay createMTinkerBrickletDualRelayFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletDualRelay)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletDualRelayToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public BrickletIndustrialQuadRelay createMTinkerBrickletIndustrialQuadRelayFromString(EDataType eDataType, String initialValue)
  {
    return (BrickletIndustrialQuadRelay)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertMTinkerBrickletIndustrialQuadRelayToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ModelPackage getModelPackage()
  {
    return (ModelPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static ModelPackage getPackage()
  {
    return ModelPackage.eINSTANCE;
  }

} //ModelFactoryImpl

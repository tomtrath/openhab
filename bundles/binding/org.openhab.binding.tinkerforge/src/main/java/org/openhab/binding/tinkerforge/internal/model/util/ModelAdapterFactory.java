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
package org.openhab.binding.tinkerforge.internal.model.util;

import com.tinkerforge.Device;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.openhab.binding.tinkerforge.internal.model.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.openhab.binding.tinkerforge.internal.model.ModelPackage
 * @generated
 */
public class ModelAdapterFactory extends AdapterFactoryImpl
{
  /**
   * The cached model package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static ModelPackage modelPackage;

  /**
   * Creates an instance of the adapter factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ModelAdapterFactory()
  {
    if (modelPackage == null)
    {
      modelPackage = ModelPackage.eINSTANCE;
    }
  }

  /**
   * Returns whether this factory is applicable for the type of the object.
   * <!-- begin-user-doc -->
   * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
   * <!-- end-user-doc -->
   * @return whether this factory is applicable for the type of the object.
   * @generated
   */
  @Override
  public boolean isFactoryForType(Object object)
  {
    if (object == modelPackage)
    {
      return true;
    }
    if (object instanceof EObject)
    {
      return ((EObject)object).eClass().getEPackage() == modelPackage;
    }
    return false;
  }

  /**
   * The switch that delegates to the <code>createXXX</code> methods.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ModelSwitch<Adapter> modelSwitch =
    new ModelSwitch<Adapter>()
    {
      @Override
      public Adapter caseTFConfig(TFConfig object)
      {
        return createTFConfigAdapter();
      }
      @Override
      public <TFC extends TFConfig> Adapter caseOHTFDevice(OHTFDevice<TFC> object)
      {
        return createOHTFDeviceAdapter();
      }
      @Override
      public Adapter caseOHConfig(OHConfig object)
      {
        return createOHConfigAdapter();
      }
      @Override
      public Adapter caseEcosystem(Ecosystem object)
      {
        return createEcosystemAdapter();
      }
      @Override
      public Adapter caseMBrickd(MBrickd object)
      {
        return createMBrickdAdapter();
      }
      @Override
      public <TFC> Adapter caseMTFConfigConsumer(MTFConfigConsumer<TFC> object)
      {
        return createMTFConfigConsumerAdapter();
      }
      @Override
      public Adapter caseMBaseDevice(MBaseDevice object)
      {
        return createMBaseDeviceAdapter();
      }
      @Override
      public <TF extends Device> Adapter caseMDevice(MDevice<TF> object)
      {
        return createMDeviceAdapter();
      }
      @Override
      public <S extends MSubDevice<?>> Adapter caseMSubDeviceHolder(MSubDeviceHolder<S> object)
      {
        return createMSubDeviceHolderAdapter();
      }
      @Override
      public Adapter caseMBrickServo(MBrickServo object)
      {
        return createMBrickServoAdapter();
      }
      @Override
      public Adapter caseTFBrickDCConfiguration(TFBrickDCConfiguration object)
      {
        return createTFBrickDCConfigurationAdapter();
      }
      @Override
      public Adapter caseMBrickDC(MBrickDC object)
      {
        return createMBrickDCAdapter();
      }
      @Override
      public Adapter caseMDualRelayBricklet(MDualRelayBricklet object)
      {
        return createMDualRelayBrickletAdapter();
      }
      @Override
      public Adapter caseMIndustrialQuadRelayBricklet(MIndustrialQuadRelayBricklet object)
      {
        return createMIndustrialQuadRelayBrickletAdapter();
      }
      @Override
      public Adapter caseMIndustrialQuadRelay(MIndustrialQuadRelay object)
      {
        return createMIndustrialQuadRelayAdapter();
      }
      @Override
      public Adapter caseMActor(MActor object)
      {
        return createMActorAdapter();
      }
      @Override
      public Adapter caseMSwitchActor(MSwitchActor object)
      {
        return createMSwitchActorAdapter();
      }
      @Override
      public Adapter caseMOutSwitchActor(MOutSwitchActor object)
      {
        return createMOutSwitchActorAdapter();
      }
      @Override
      public Adapter caseMInSwitchActor(MInSwitchActor object)
      {
        return createMInSwitchActorAdapter();
      }
      @Override
      public <B extends MSubDeviceHolder<?>> Adapter caseMSubDevice(MSubDevice<B> object)
      {
        return createMSubDeviceAdapter();
      }
      @Override
      public Adapter caseMDualRelay(MDualRelay object)
      {
        return createMDualRelayAdapter();
      }
      @Override
      public Adapter caseTFNullConfiguration(TFNullConfiguration object)
      {
        return createTFNullConfigurationAdapter();
      }
      @Override
      public Adapter caseTFServoConfiguration(TFServoConfiguration object)
      {
        return createTFServoConfigurationAdapter();
      }
      @Override
      public Adapter caseMServo(MServo object)
      {
        return createMServoAdapter();
      }
      @Override
      public Adapter caseMSensor(MSensor object)
      {
        return createMSensorAdapter();
      }
      @Override
      public Adapter caseMBrickletHumidity(MBrickletHumidity object)
      {
        return createMBrickletHumidityAdapter();
      }
      @Override
      public Adapter caseMBrickletDistanceIR(MBrickletDistanceIR object)
      {
        return createMBrickletDistanceIRAdapter();
      }
      @Override
      public Adapter caseMBrickletTemperature(MBrickletTemperature object)
      {
        return createMBrickletTemperatureAdapter();
      }
      @Override
      public Adapter caseTFBaseConfiguration(TFBaseConfiguration object)
      {
        return createTFBaseConfigurationAdapter();
      }
      @Override
      public Adapter caseMBrickletBarometer(MBrickletBarometer object)
      {
        return createMBrickletBarometerAdapter();
      }
      @Override
      public Adapter caseMBarometerTemperature(MBarometerTemperature object)
      {
        return createMBarometerTemperatureAdapter();
      }
      @Override
      public Adapter caseMBrickletAmbientLight(MBrickletAmbientLight object)
      {
        return createMBrickletAmbientLightAdapter();
      }
      @Override
      public Adapter caseMBrickletLCD20x4(MBrickletLCD20x4 object)
      {
        return createMBrickletLCD20x4Adapter();
      }
      @Override
      public Adapter caseMTextActor(MTextActor object)
      {
        return createMTextActorAdapter();
      }
      @Override
      public Adapter caseMLCD20x4Button(MLCD20x4Button object)
      {
        return createMLCD20x4ButtonAdapter();
      }
      @Override
      public Adapter defaultCase(EObject object)
      {
        return createEObjectAdapter();
      }
    };

  /**
   * Creates an adapter for the <code>target</code>.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param target the object to adapt.
   * @return the adapter for the <code>target</code>.
   * @generated
   */
  @Override
  public Adapter createAdapter(Notifier target)
  {
    return modelSwitch.doSwitch((EObject)target);
  }


  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.TFConfig <em>TF Config</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.TFConfig
   * @generated
   */
  public Adapter createTFConfigAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.OHTFDevice <em>OHTF Device</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.OHTFDevice
   * @generated
   */
  public Adapter createOHTFDeviceAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.OHConfig <em>OH Config</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.OHConfig
   * @generated
   */
  public Adapter createOHConfigAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.Ecosystem <em>Ecosystem</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.Ecosystem
   * @generated
   */
  public Adapter createEcosystemAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickd <em>MBrickd</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickd
   * @generated
   */
  public Adapter createMBrickdAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MTFConfigConsumer <em>MTF Config Consumer</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MTFConfigConsumer
   * @generated
   */
  public Adapter createMTFConfigConsumerAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBaseDevice <em>MBase Device</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBaseDevice
   * @generated
   */
  public Adapter createMBaseDeviceAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MDevice <em>MDevice</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MDevice
   * @generated
   */
  public Adapter createMDeviceAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MSubDeviceHolder <em>MSub Device Holder</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MSubDeviceHolder
   * @generated
   */
  public Adapter createMSubDeviceHolderAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickServo <em>MBrick Servo</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickServo
   * @generated
   */
  public Adapter createMBrickServoAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.TFBrickDCConfiguration <em>TF Brick DC Configuration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.TFBrickDCConfiguration
   * @generated
   */
  public Adapter createTFBrickDCConfigurationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickDC <em>MBrick DC</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickDC
   * @generated
   */
  public Adapter createMBrickDCAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MDualRelayBricklet <em>MDual Relay Bricklet</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MDualRelayBricklet
   * @generated
   */
  public Adapter createMDualRelayBrickletAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MIndustrialQuadRelayBricklet <em>MIndustrial Quad Relay Bricklet</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MIndustrialQuadRelayBricklet
   * @generated
   */
  public Adapter createMIndustrialQuadRelayBrickletAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MIndustrialQuadRelay <em>MIndustrial Quad Relay</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MIndustrialQuadRelay
   * @generated
   */
  public Adapter createMIndustrialQuadRelayAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MActor <em>MActor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MActor
   * @generated
   */
  public Adapter createMActorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MSwitchActor <em>MSwitch Actor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MSwitchActor
   * @generated
   */
  public Adapter createMSwitchActorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MOutSwitchActor <em>MOut Switch Actor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MOutSwitchActor
   * @generated
   */
  public Adapter createMOutSwitchActorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MInSwitchActor <em>MIn Switch Actor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MInSwitchActor
   * @generated
   */
  public Adapter createMInSwitchActorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MSubDevice <em>MSub Device</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MSubDevice
   * @generated
   */
  public Adapter createMSubDeviceAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MDualRelay <em>MDual Relay</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MDualRelay
   * @generated
   */
  public Adapter createMDualRelayAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.TFNullConfiguration <em>TF Null Configuration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.TFNullConfiguration
   * @generated
   */
  public Adapter createTFNullConfigurationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.TFServoConfiguration <em>TF Servo Configuration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.TFServoConfiguration
   * @generated
   */
  public Adapter createTFServoConfigurationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MServo <em>MServo</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MServo
   * @generated
   */
  public Adapter createMServoAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MSensor <em>MSensor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MSensor
   * @generated
   */
  public Adapter createMSensorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletHumidity <em>MBricklet Humidity</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletHumidity
   * @generated
   */
  public Adapter createMBrickletHumidityAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletDistanceIR <em>MBricklet Distance IR</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletDistanceIR
   * @generated
   */
  public Adapter createMBrickletDistanceIRAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletTemperature <em>MBricklet Temperature</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletTemperature
   * @generated
   */
  public Adapter createMBrickletTemperatureAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.TFBaseConfiguration <em>TF Base Configuration</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.TFBaseConfiguration
   * @generated
   */
  public Adapter createTFBaseConfigurationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletBarometer <em>MBricklet Barometer</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletBarometer
   * @generated
   */
  public Adapter createMBrickletBarometerAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBarometerTemperature <em>MBarometer Temperature</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBarometerTemperature
   * @generated
   */
  public Adapter createMBarometerTemperatureAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletAmbientLight <em>MBricklet Ambient Light</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletAmbientLight
   * @generated
   */
  public Adapter createMBrickletAmbientLightAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MBrickletLCD20x4 <em>MBricklet LCD2 0x4</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MBrickletLCD20x4
   * @generated
   */
  public Adapter createMBrickletLCD20x4Adapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MTextActor <em>MText Actor</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MTextActor
   * @generated
   */
  public Adapter createMTextActorAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link org.openhab.binding.tinkerforge.internal.model.MLCD20x4Button <em>MLCD2 0x4 Button</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see org.openhab.binding.tinkerforge.internal.model.MLCD20x4Button
   * @generated
   */
  public Adapter createMLCD20x4ButtonAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for the default case.
   * <!-- begin-user-doc -->
   * This default implementation returns null.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @generated
   */
  public Adapter createEObjectAdapter()
  {
    return null;
  }

} //ModelAdapterFactory

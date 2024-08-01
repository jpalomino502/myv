package com.example.oscarapp.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ServiceRequest(
    @SerializedName("ticket_id") val ticketId: String,
    @SerializedName("service_control") val serviceControl: String,
    @SerializedName("tipo_servicio") val tipoServicio: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("fecha") val fecha: Date,
    @SerializedName("razon_social") val razonSocial: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("nit") val nit: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("celular") val celular: String,
    @SerializedName("observaciones1") val observaciones1: String,
    @SerializedName("horaingreso") val horaingreso: String,
    @SerializedName("horasalida") val horasalida: String,
    @SerializedName("producto") val producto: String,
    @SerializedName("dosificacion") val dosificacion: String,
    @SerializedName("concentracion") val concentracion: String,
    @SerializedName("cantidad") val cantidad: String,
    @SerializedName("observaciones2") val observaciones2: String,
    @SerializedName("valortotal") val valortotal: String,
    @SerializedName("tipopago") val tipopago: String,
    @SerializedName("fechavencimiento") val fechavencimiento: Date?,
    @SerializedName("fecharealizar") val fecharealizar: Date?,
    @SerializedName("fechaproximo") val fechaproximo: Date?,
    @SerializedName("autorizacion_cliente") val autorizacion_cliente: String,
    @SerializedName("nombre_asesor") val nombre_asesor: String,
    @SerializedName("recibi_cliente") val recibi_cliente: String,
    @SerializedName("nombre_tecnico") val nombre_tecnico: String,
    @SerializedName("ulr_ratones") val ulr_ratones: String,
    @SerializedName("firma") val firma: String,
    @SerializedName("observaciones3") val observaciones3: String,
    @SerializedName("informeserviciojson") val informeserviciojson: String,
    @SerializedName("foto_novedad") val foto_novedad: String
)
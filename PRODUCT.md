# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Food businesses using the app to handle orders, payments, and daily records. The current app has separate admin and employee flows; the exact responsibilities of each role remain open.

## Product Purpose

Cod Cashier helps food businesses record orders and payments and keep track of daily activity.

## Operating Context

The Android app is used by admins and employees. Its current navigation includes order and payment details, transaction history, expense entry, deposits, and recap screens.

## Capabilities and Constraints

- The existing implementation includes menu, category, subcategory, employee, deposit, business profile, transaction, and recap management.
- The app uses a remote API for its business data and includes PDF receipt and recap code.
- The product is intended for multiple food businesses. Tenant setup, data separation, and deployment arrangements are not yet confirmed.

## Brand Commitments

The app name is Cod Cashier. The current interface uses Indonesian business terms, including *pegawai*, *warung*, *transaksi*, *pengeluaran*, and *rekap*. Whether Indonesian is the only supported language remains open.

## Evidence on Hand

The repository contains an Android app built with Kotlin and Jetpack Compose, with role-specific navigation and the workflows listed above. No product claims, customer proof, or measured outcomes were provided.

## Product Principles

- Keep order and payment work clear for staff during service.
- Make daily records understandable to the business owner or admin.
- Preserve the distinction between admin and employee tasks where their permissions differ.

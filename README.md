# Impersonations Hook

This is a module for Liferay (version 7.1) that implements a more effective impersonation function without using an url parameter

> *IMPORTANT!* This project is still under development and it doesn't work yet. You can see a fully functionaly hook plugin for 6.2 version in [carlosdurannet/impersonation-hook](https://github.com/carlosdurannet/impersonation-hook) repository

## Requirements

The module needs a Liferay Portal and user must access to manual login using screen name

## Usage

Once the hook is deployed, user must access to login using as screen name this format

```
realuser#impersonateduser
```
### Example
If user `jhond` wants to impersonate user `janed` the value for the screen name field must be as follows

```
johnd#janed
```

The password to use is always the real user one.

## Managing access to impersonation

For security reasons, this functionality is not open without restrictions. The access to use it **is controlled by a portal role** that is configurable.

The default name for this role is **ImpersonationUser**, so you must to create a portal role with that name and assign to it the users you want to access impersonation.

To use a different role, you must to add the property `impersonation-role` in the **portal-ext.properties** file with the custom name of the role. It must be something like this:

```
impersonation-role=AnotherName
```
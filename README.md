# Impersonations Module

This is a module for Liferay 7 that implements a more effective impersonation function without using an url parameter

## Requirements

The module needs a Liferay 7.0.x Portal with [impersonation-sb module](https://github.com/carlosdurannet/impersonation-sb) deployed

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
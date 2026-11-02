import {inject} from '@loopback/core';
import {post, requestBody} from '@loopback/rest';
import {NotificationService} from '../services';

const messageProperties = {
  title: {type: 'string', minLength: 1},
  body: {type: 'string', minLength: 1},
} as const;

export class NotificationController {
  constructor(
    @inject('services.NotificationService')
    private notificationService: NotificationService,
  ) {}

  @post('/notifications/topic', {
    summary: 'Send a push notification to all devices subscribed to a topic',
    responses: {
      '200': {
        description: 'Firebase message id',
        content: {'application/json': {schema: {type: 'object'}}},
      },
    },
  })
  async sendToTopic(
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['title', 'body'],
            properties: {
              topic: {type: 'string', default: 'portfolio'},
              ...messageProperties,
            },
          },
          example: {
            topic: 'portfolio',
            title: 'Hello',
            body: 'Sent from LoopBack',
          },
        },
      },
    })
    message: {
      topic?: string;
      title: string;
      body: string;
    },
  ): Promise<{messageId: string}> {
    const messageId = await this.notificationService.sendToTopic(
      message.topic ?? 'portfolio',
      message.title,
      message.body,
    );
    return {messageId};
  }

  @post('/notifications/device', {
    summary: 'Send a push notification to one device (FCM token)',
    responses: {
      '200': {
        description: 'Firebase message id',
        content: {'application/json': {schema: {type: 'object'}}},
      },
    },
  })
  async sendToDevice(
    @requestBody({
      content: {
        'application/json': {
          schema: {
            type: 'object',
            required: ['token', 'title', 'body'],
            properties: {
              token: {type: 'string', minLength: 1},
              ...messageProperties,
            },
          },
        },
      },
    })
    message: {
      token: string;
      title: string;
      body: string;
    },
  ): Promise<{messageId: string}> {
    const messageId = await this.notificationService.sendToDevice(
      message.token,
      message.title,
      message.body,
    );
    return {messageId};
  }
}
